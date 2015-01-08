package cz.jakubmaly.pxslt;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;


public class StreamUtils {
    public static String getString(InputStream tr) throws IOException {
        int i;
        String s = "";
        do {
            i = tr.read();
            if (i != -1) {
                s += (char) i;
            }
        } while (i != -1);
        return s;
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
