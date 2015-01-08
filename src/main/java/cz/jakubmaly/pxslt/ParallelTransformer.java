package cz.jakubmaly.pxslt;

import net.sf.saxon.s9api.*;
import org.apache.commons.io.FileUtils;

import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamSource;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;

public class ParallelTransformer {

    private XsltCompiler compiler;
    private Processor processor;
    private XsltExecutable xsltExecutable;
    private Integer parallelism;
    private TransformerInitializer transformerInitializer;

    public ParallelTransformer(InputStream xsltStylesheet) throws IOException, SaxonApiException {
        processor = new Processor(true);
        compiler = processor.newXsltCompiler();
        compiler.setXsltLanguageVersion("3.0");
        xsltExecutable = compiler.compile(new StreamSource(xsltStylesheet));
    }

    private void transform(SlicedInput slicedInput, ResultMerger resultMerger) throws TransformerException, SaxonApiException, IOException {
        MasterAction masterAction = new MasterAction(xsltExecutable, slicedInput.getSources());
        ForkJoinPool pool = getParallelism() != null ? new ForkJoinPool(getParallelism()) : new ForkJoinPool();
        pool.invoke(masterAction);

        resultMerger.init();
        for (TransformationPartAction task : masterAction.transformationParts) {
            task.join();
            resultMerger.append(task.partOutputTempFile);
            task.cleanup();
        }
        resultMerger.complete();
    }

    public TransformerInitializer getTransformerInitializer() {
        return transformerInitializer;
    }

    public void setTransformerInitializer(TransformerInitializer transformerInitializer) {
        this.transformerInitializer = transformerInitializer;
    }

    private class MasterAction extends RecursiveAction {
        private XsltExecutable xsltExecutable;
        private Iterable<Source> sources;
        List<TransformationPartAction> transformationParts = new ArrayList<>();

        public MasterAction(XsltExecutable xsltExecutable, Iterable<Source> sources) {
            this.xsltExecutable = xsltExecutable;
            this.sources = sources;
        }

        @Override
        protected void compute() {
            try {
                int partNo = 1;
                for (Source source : sources) {
                    XsltTransformer transformer = xsltExecutable.load();
                    transformer.setInitialMode(new QName("stream"));
                    if (getTransformerInitializer() != null) {
                        getTransformerInitializer().initialize(transformer);
                    }
                    TransformationPartAction transformationPartAction = new TransformationPartAction(source, transformer, partNo++);
                    transformationPartAction.fork();
                    transformationParts.add(transformationPartAction);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private class TransformationPartAction extends RecursiveAction {
        private XsltTransformer transformer;
        private boolean error;
        File partOutputTempFile;
        int partNo;

        private TransformationPartAction(Source input, XsltTransformer transformer, int partNo) throws SaxonApiException, IOException {
            this.partNo = partNo;
            partOutputTempFile = File.createTempFile("pxslt", ".tmp");
            Serializer output = processor.newSerializer(partOutputTempFile);
            this.transformer = transformer;
            transformer.setSource(input);
            transformer.setDestination(output);
            if (partNo > 1){
                output.setOutputProperty(Serializer.Property.OMIT_XML_DECLARATION, "yes");
            }
        }

        @Override
        protected void compute() {
            try {
                System.out.println("Starting part: " + partNo);
                transformer.transform();
                System.out.println("Finished part: " + partNo);
            } catch (SaxonApiException e) {
                System.out.println("Failed part: " + partNo);
                System.out.println(e);
                error = true;
            }
        }

        public void cleanup() {
            FileUtils.deleteQuietly(partOutputTempFile);
        }
    }

    public void transform(SlicedInput slicedInput, final FileOutputStream result) throws TransformerException, SaxonApiException, IOException {
        transform(slicedInput, new FileStreamResultMerger(result));
    }

    public void transform(SlicedInput slicedInput, final Writer result) throws TransformerException, SaxonApiException, IOException {
        transform(slicedInput, new WriterResultMerger(result));
    }

    public Integer getParallelism() {
        return parallelism;
    }

    public void setParallelism(Integer parallelism) {
        this.parallelism = parallelism;
    }
}



