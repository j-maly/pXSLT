package cz.jakubmaly.pxslt;

import cz.jakubmaly.pxslt.utils.TrimmingStream;
import cz.jakubmaly.pxslt.utils.WrappingStream;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Splits a file into chunks that can be processed in parallel
 */
public class Slicer {
    public SlicedInput sliceStream(File inputFile, SlicingHints slicingHints) throws IOException {
        if (slicingHints != null) {
            int[] starts = slicingHints.getStarts();
            InputStream[] result = new WrappingStream[slicingHints.getStarts().length];
            for (int i = 0; i < starts.length; i++) {
                int start = starts[i];
                int end = i < starts.length - 1 ? starts[i + 1] : -1;
                FileInputStream rawStream = new FileInputStream(inputFile);
                TrimmingStream trimmedStream = new TrimmingStream(rawStream, start, end);
                WrappingStream wrappedStream = new WrappingStream(trimmedStream, slicingHints.getPrefixes()[i], slicingHints.getSuffixes()[i]);
                result[i] = wrappedStream;
            }
            return new SlicedInput(result);
        } else {
            return new SlicedInput(new InputStream[]{new FileInputStream(inputFile)});
        }
    }

    public static String getStringWithBuffer(InputStream tr) throws IOException {
        String result = "";
        java.util.List<String> readLines = IOUtils.readLines(tr);
        for (int i = 0; i < readLines.size(); i++) {
            String s = readLines.get(i);
            result += s;
            if (i < readLines.size() - 1) {
                result += "\r\n";
            }
        }
        return result;
    }
}