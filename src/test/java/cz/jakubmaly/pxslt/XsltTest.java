package cz.jakubmaly.pxslt;

import net.sf.saxon.s9api.*;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.time.StopWatch;
import org.custommonkey.xmlunit.XMLAssert;
import org.custommonkey.xmlunit.XMLUnit;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.xml.sax.SAXException;

import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamSource;
import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;

public class XsltTest {

    private XsltTransformer booksTransformer;
    private XsltTransformer proteinsTransformer;
    private XsltTransformer ruianTransformer;
    private Processor processor;
    private XsltCompiler comp;

    @Before
    public void Setup() throws TransformerException, SaxonApiException {
        processor = new Processor(true);
        comp = processor.newXsltCompiler();
        comp.setXsltLanguageVersion("3.0");
        booksTransformer = initializeTransformer("/xslt/books.xslt");
        proteinsTransformer = initializeTransformer("/xslt/proteins_stream.xslt");
        ruianTransformer = initializeTransformer("/xslt/ruian_split.xslt");
        ruianTransformer.setParameter(new QName("output-folder"), new XdmAtomicValue("/d:/GitHub/pXSLT/target/test-output/split"));
    }

    XsltTransformer initializeTransformer(String xsltFilePath) throws TransformerException, SaxonApiException {
        InputStream xsltStream = XsltTest.class.getResourceAsStream(xsltFilePath);
        try {
            StreamSource xsltSource = new StreamSource(xsltStream);
            XsltExecutable exp = comp.compile(xsltSource);
            XsltTransformer transformer = exp.load();
            return transformer;
        } finally {
            IOUtils.closeQuietly(xsltStream);
        }
    }

    @Test
    public void transformBooks() throws TransformerException, SaxonApiException {
        doTransform("/xml/books.xml", booksTransformer, null);
    }

    @Test
    public void transformProteinsSampleSingleThread() throws TransformerException, SaxonApiException {
        String xml = doTransform("/xml/proteins_sample.xml", proteinsTransformer, null);
        System.out.println(xml);
    }

    @Test
    public void transformRuianSampleSingleThread() throws TransformerException, SaxonApiException {
        ruianTransformer.setInitialMode(new QName("stream"));
        String xml = doTransform("/xml/ruian_sample.xml", ruianTransformer, null);
        System.out.println(xml);
    }

    @Test
    @Ignore
    public void transformProteinsFullSingleThread() throws TransformerException, IOException, SaxonApiException {
        XsltExecutable exp = comp.compile(new StreamSource(XsltTest.class.getResourceAsStream("/xslt/proteins_stream.xslt")));
        StreamSource inputSource = new StreamSource(XsltTest.class.getResourceAsStream(("/xml/proteins.xml")));
        Serializer out = processor.newSerializer(new FileWriter("target/test-output/proteins.single.out.xml"));
        XsltTransformer trans = exp.load();
        trans.setSource(inputSource);
        trans.setInitialMode(new QName("stream"));
        trans.setDestination(out);
        StopWatch w = new StopWatch();
        w.start();
        trans.transform();
        System.out.println("Time elapsed: " + w.toString());
    }

    @Test
    public void transformRuianFullSingleThread() throws TransformerException, SaxonApiException {
        ruianTransformer.setInitialMode(new QName("stream"));
        String xml = doTransform("/xml/ruian_prague.xml", ruianTransformer, null);
        System.out.println(xml);
    }

    private String doTransform(String resourcePath, XsltTransformer transformer, Writer writer) throws TransformerException, SaxonApiException {
        InputStream inputStream = XsltTest.class.getResourceAsStream(resourcePath);
        return doTransform(inputStream, transformer, writer);
    }

    private String doTransform(InputStream inputStream, XsltTransformer transformer, Writer writer) throws SaxonApiException {
        try {
            StreamSource inputSource = new StreamSource(inputStream);
            if (writer == null)
                writer = new StringWriter();
            Serializer out = processor.newSerializer(writer);
            transformer.setSource(inputSource);
            transformer.setDestination(out);
            StopWatch w = new StopWatch();
            w.start();
            transformer.transform();
            System.out.println("Time elapsed: " + w.toString());
            String result = writer.toString();
            return result;
        } finally {
            IOUtils.closeQuietly(inputStream);
        }
    }

    @Test
    public void transformBooksParallel() throws TransformerException, IOException, SAXException, URISyntaxException, SaxonApiException {
        InputStream xsltStream = XsltTest.class.getResourceAsStream("/xslt/books.xslt");
        String result;
        try {
            Slicer s = new Slicer();
            URL url = XsltTest.class.getResource("/xml/books.xml");
            File inputFile = new File(url.toURI());
            SlicedInput slicedInput = s.sliceStream(inputFile, null);
            ParallelTransformer pTransformer = new ParallelTransformer(xsltStream);
            StringWriter parallelResult = new StringWriter();
            pTransformer.transform(slicedInput, parallelResult);
            result = parallelResult.toString();
        } finally {
            IOUtils.closeQuietly(xsltStream);
        }

        String control = doTransform("/xml/books.xml", booksTransformer, null);
        XMLUnit.setIgnoreWhitespace(true);
        XMLAssert.assertXMLEqual(control, result);
        System.out.println(control);
    }

    @Test(expected = org.xml.sax.SAXParseException.class)
    public void transformBooksParallelWithSlicing() throws TransformerException, IOException, SAXException, URISyntaxException, SaxonApiException {
        InputStream xsltStream = XsltTest.class.getResourceAsStream("/xslt/books.xslt");
        String result;
        try {
            Slicer s = new Slicer();
            String prefix = "<?xml version=\"1.0\" encoding=\"utf-8\"?><BookStore>";
            String suffix = "</BookStore>";
            String[] prefixes = new String[] {null, prefix, prefix};
            String[] suffixes = new String[] {suffix, suffix, null};
            SlicingHints hints = new SlicingHints(new int[] { 0, 280, 531 }, prefixes, suffixes);
            URL url = XsltTest.class.getResource("/xml/books.xml");
            File inputFile = new File(url.toURI());
            SlicedInput slicedInput = s.sliceStream(inputFile, hints);
            ParallelTransformer pTransformer = new ParallelTransformer(xsltStream);
            StringWriter parallelResult = new StringWriter();
            pTransformer.transform(slicedInput, parallelResult);
            result = parallelResult.toString();
        } finally {
            IOUtils.closeQuietly(xsltStream);
        }

        String control = doTransform("/xml/books.xml", booksTransformer, null);
        System.out.println(result);
        XMLUnit.setIgnoreWhitespace(true);
        XMLAssert.assertXMLEqual(control, result);
    }

    @Test(expected = org.xml.sax.SAXParseException.class)
    public void transformProteinsSampleParallelWithSlicing() throws TransformerException, IOException, SAXException, URISyntaxException, SaxonApiException {
        InputStream xsltStream = XsltTest.class.getResourceAsStream("/xslt/proteins_stream.xslt");
        proteinsTransformer.setInitialMode(new QName("stream"));
        String result;
        try {
            Slicer s = new Slicer();
            String prefix = "<?xml version=\"1.0\" encoding=\"utf-8\"?><ProteinDatabase>";
            String suffix = "</ProteinDatabase>";
            String[] prefixes = new String[] {null, prefix, prefix, prefix};
            String[] suffixes = new String[] {suffix, suffix, suffix, null};
            SlicingHints hints = new SlicingHints(new int[] { 0, 16_433, 32_961, 47_515 }, prefixes, suffixes);
            URL url = XsltTest.class.getResource("/xml/proteins_sample.xml");
            File inputFile = new File(url.toURI());
            SlicedInput slicedInput = s.sliceStream(inputFile, hints);
            //doTransform(slicedInput, proteinsTransformer);
            //slicedInput.debugPrint("<ProteinEntry");
            ParallelTransformer pTransformer = new ParallelTransformer(xsltStream);
            StringWriter parallelResult = new StringWriter();
            pTransformer.transform(slicedInput, parallelResult);
            result = parallelResult.toString();
            FileUtils.writeStringToFile(new File("target/test-output/result.xml"), result);
        } finally {
            IOUtils.closeQuietly(xsltStream);
        }

        String control = doTransform("/xml/proteins_sample.xml", proteinsTransformer, null);
        FileUtils.writeStringToFile(new File("target/test-output/control.xml"), control);
        System.out.println(result);
        XMLUnit.setIgnoreWhitespace(true);
        XMLAssert.assertXMLEqual(control, result);
    }

    @Test
    public void transformProteinsFullParallelWithSlicing() throws TransformerException, IOException, SAXException, URISyntaxException, SaxonApiException {
        InputStream xsltStream = XsltTest.class.getResourceAsStream("/xslt/proteins_stream.xslt");
        proteinsTransformer.setInitialMode(new QName("stream"));
        try {
            Slicer s = new Slicer();
            String prefix = "<?xml version=\"1.0\" encoding=\"utf-8\"?><ProteinDatabase>";
            String suffix = "</ProteinDatabase>";
//            String[] prefixes = new String[] {null, prefix, prefix, prefix, prefix, prefix, prefix};
//            String[] suffixes = new String[] {suffix, suffix, suffix, suffix, suffix, suffix, null};
//            SlicingHints hints = new SlicingHints(new int[] { 0, 90_003_233, 180_003_205, 270_003_313, 360_002_502, 450_003_948, 540_001_707}, prefixes, suffixes);
            String[] prefixes = new String[] {null, prefix, prefix, prefix};
            String[] suffixes = new String[] {suffix, suffix, suffix, null};
            SlicingHints hints = new SlicingHints(new int[] { 0, 180_003_205, 360_002_502, 540_001_707}, prefixes, suffixes);
            URL url = XsltTest.class.getResource("/xml/proteins.xml");
            File inputFile = new File(url.toURI());
            SlicedInput slicedInput = s.sliceStream(inputFile, hints);
            //slicedInput.debugPrint("<ProteinEntry");
            ParallelTransformer pTransformer = new ParallelTransformer(xsltStream);
            pTransformer.setParallelism(4);
            FileOutputStream result = new FileOutputStream(new File("target/test-output/proteins.parallel.out.xml"));
            StopWatch w = new StopWatch();
            w.start();
            pTransformer.transform(slicedInput, result);
            System.out.println("Time elapsed: " + w.toString());
        } finally {
            IOUtils.closeQuietly(xsltStream);
        }
    }

    @Test
    public void transformRuianSampleParallelWithSlicing() throws TransformerException, IOException, SAXException, URISyntaxException, SaxonApiException {
        InputStream xsltStream = XsltTest.class.getResourceAsStream("/xslt/ruian_split.xslt");
        ruianTransformer.setInitialMode(new QName("stream"));
        try {
            Slicer s = new Slicer();
            String prefix = "<?xml version=\"1.0\" encoding=\"utf-8\"?><vf:VymennyFormat xmlns:gml=\"http://www.opengis.net/gml/3.2\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:ami=\"urn:cz:isvs:ruian:schemas:AdrMistoIntTypy:v1\" xmlns:base=\"urn:cz:isvs:ruian:schemas:BaseTypy:v1\" xmlns:coi=\"urn:cz:isvs:ruian:schemas:CastObceIntTypy:v1\" xmlns:com=\"urn:cz:isvs:ruian:schemas:CommonTypy:v1\" xmlns:kui=\"urn:cz:isvs:ruian:schemas:KatUzIntTypy:v1\" xmlns:kri=\"urn:cz:isvs:ruian:schemas:KrajIntTypy:v1\" xmlns:mci=\"urn:cz:isvs:ruian:schemas:MomcIntTypy:v1\" xmlns:mpi=\"urn:cz:isvs:ruian:schemas:MopIntTypy:v1\" xmlns:obi=\"urn:cz:isvs:ruian:schemas:ObecIntTypy:v1\" xmlns:oki=\"urn:cz:isvs:ruian:schemas:OkresIntTypy:v1\" xmlns:opi=\"urn:cz:isvs:ruian:schemas:OrpIntTypy:v1\" xmlns:pai=\"urn:cz:isvs:ruian:schemas:ParcelaIntTypy:v1\" xmlns:pui=\"urn:cz:isvs:ruian:schemas:PouIntTypy:v1\" xmlns:rsi=\"urn:cz:isvs:ruian:schemas:RegSouIntiTypy:v1\" xmlns:spi=\"urn:cz:isvs:ruian:schemas:SpravObvIntTypy:v1\" xmlns:sti=\"urn:cz:isvs:ruian:schemas:StatIntTypy:v1\" xmlns:soi=\"urn:cz:isvs:ruian:schemas:StavObjIntTypy:v1\" xmlns:uli=\"urn:cz:isvs:ruian:schemas:UliceIntTypy:v1\" xmlns:vci=\"urn:cz:isvs:ruian:schemas:VuscIntTypy:v1\" xmlns:vf=\"urn:cz:isvs:ruian:schemas:VymennyFormatTypy:v1\" xmlns:zji=\"urn:cz:isvs:ruian:schemas:ZsjIntTypy:v1\"><vf:Data>";
            //String prefix = "XXXX";
            String suffix = "</vf:Data></vf:VymennyFormat>";
            String[] prefixes = new String[] {
                    null,
                    prefix + "<vf:Parcely>",
                    prefix + "<vf:Parcely>",
                    prefix + "<vf:StavebniObjekty>"};
            String[] suffixes = new String[] {
                    "</vf:Parcely>" + suffix,
                    "</vf:Parcely>" + suffix,
                    "</vf:StavebniObjekty>" + suffix, null};
            SlicingHints hints = new SlicingHints(new int[] { 0, 750_036, 1_500_032, 2_250_513 }, prefixes, suffixes);
            URL url = XsltTest.class.getResource("/xml/ruian_sample.xml");
            File inputFile = new File(url.toURI());
            SlicedInput slicedInput = s.sliceStream(inputFile, hints);
            //doTransform(slicedInput, ruianTransformer);
            ParallelTransformer pTransformer = new ParallelTransformer(xsltStream);
            pTransformer.setTransformerInitializer(new TransformerInitializer() {
                @Override
                public void initialize(XsltTransformer transformer) {
                    transformer.setParameter(new QName("output-folder"), new XdmAtomicValue("/d:/GitHub/pXSLT/target/test-output/split"));
                }
            });
            pTransformer.setParallelism(4);
            FileOutputStream result = new FileOutputStream(new File("target/test-output/ruian_sample.parallel.out.xml"));
            StopWatch w = new StopWatch();
            w.start();
            pTransformer.transform(slicedInput, result);
            System.out.println("Time elapsed: " + w.toString());
        } finally {
            IOUtils.closeQuietly(xsltStream);
        }
    }

    @Test
    public void transformRuianFullParallelWithSlicing() throws TransformerException, IOException, SAXException, URISyntaxException, SaxonApiException {
        InputStream xsltStream = XsltTest.class.getResourceAsStream("/xslt/ruian_split.xslt");
        ruianTransformer.setInitialMode(new QName("stream"));
        try {
            Slicer s = new Slicer();
            String prefix = "<?xml version=\"1.0\" encoding=\"utf-8\"?><vf:VymennyFormat xmlns:gml=\"http://www.opengis.net/gml/3.2\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:ami=\"urn:cz:isvs:ruian:schemas:AdrMistoIntTypy:v1\" xmlns:base=\"urn:cz:isvs:ruian:schemas:BaseTypy:v1\" xmlns:coi=\"urn:cz:isvs:ruian:schemas:CastObceIntTypy:v1\" xmlns:com=\"urn:cz:isvs:ruian:schemas:CommonTypy:v1\" xmlns:kui=\"urn:cz:isvs:ruian:schemas:KatUzIntTypy:v1\" xmlns:kri=\"urn:cz:isvs:ruian:schemas:KrajIntTypy:v1\" xmlns:mci=\"urn:cz:isvs:ruian:schemas:MomcIntTypy:v1\" xmlns:mpi=\"urn:cz:isvs:ruian:schemas:MopIntTypy:v1\" xmlns:obi=\"urn:cz:isvs:ruian:schemas:ObecIntTypy:v1\" xmlns:oki=\"urn:cz:isvs:ruian:schemas:OkresIntTypy:v1\" xmlns:opi=\"urn:cz:isvs:ruian:schemas:OrpIntTypy:v1\" xmlns:pai=\"urn:cz:isvs:ruian:schemas:ParcelaIntTypy:v1\" xmlns:pui=\"urn:cz:isvs:ruian:schemas:PouIntTypy:v1\" xmlns:rsi=\"urn:cz:isvs:ruian:schemas:RegSouIntiTypy:v1\" xmlns:spi=\"urn:cz:isvs:ruian:schemas:SpravObvIntTypy:v1\" xmlns:sti=\"urn:cz:isvs:ruian:schemas:StatIntTypy:v1\" xmlns:soi=\"urn:cz:isvs:ruian:schemas:StavObjIntTypy:v1\" xmlns:uli=\"urn:cz:isvs:ruian:schemas:UliceIntTypy:v1\" xmlns:vci=\"urn:cz:isvs:ruian:schemas:VuscIntTypy:v1\" xmlns:vf=\"urn:cz:isvs:ruian:schemas:VymennyFormatTypy:v1\" xmlns:zji=\"urn:cz:isvs:ruian:schemas:ZsjIntTypy:v1\"><vf:Data>";
            //String prefix = "XXXX";
            String suffix = "</vf:Data></vf:VymennyFormat>";
            String[] prefixes = new String[] {
                    null,
                    prefix + "<vf:Parcely>",
                    prefix + "<vf:Parcely>",
                    prefix + "<vf:StavebniObjekty>"};
            String[] suffixes = new String[] {
                    "</vf:Parcely>" + suffix,
                    "</vf:Parcely>" + suffix,
                    "</vf:StavebniObjekty>" + suffix, null};
            SlicingHints hints = new SlicingHints(new int[] { 0, 150_000_828, 300_000_901, 450_000_079 }, prefixes, suffixes);
            URL url = XsltTest.class.getResource("/xml/ruian_prague.xml");
            File inputFile = new File(url.toURI());
            SlicedInput slicedInput = s.sliceStream(inputFile, hints);
            //slicedInput.debugPrint("<vf:");
            //doTransform(slicedInput, ruianTransformer);
            ParallelTransformer pTransformer = new ParallelTransformer(xsltStream);
            pTransformer.setTransformerInitializer(new TransformerInitializer() {
                @Override
                public void initialize(XsltTransformer transformer) {
                    transformer.setParameter(new QName("output-folder"), new XdmAtomicValue("/d:/GitHub/pXSLT/target/test-output/split"));
                }
            });
            pTransformer.setParallelism(4);
            FileOutputStream result = new FileOutputStream(new File("target/test-output/ruian_prague.parallel.out.xml"));
            StopWatch w = new StopWatch();
            w.start();
            pTransformer.transform(slicedInput, result);
            System.out.println("Time elapsed: " + w.toString());
        } finally {
            IOUtils.closeQuietly(xsltStream);
        }
    }

    private void doTransform(SlicedInput slicedInput, XsltTransformer transformer) throws SaxonApiException, IOException {
        InputStream[] inputStreams = slicedInput.inputStreams;
        for (int i = 0; i < inputStreams.length; i++) { //
            try {
                System.out.println(i);
                InputStream inputStream = inputStreams[i];
                //FileUtils.copyInputStreamToFile(inputStream, new File("target/test-output/part" + i + ".xml"));
                String part = doTransform(inputStream, transformer, null);
                FileUtils.writeStringToFile(new File("target/test-output/part" + i + ".out.xml"), part);
            } catch (Exception e) {
                System.out.println(e.toString());
            }
        }
    }
}
