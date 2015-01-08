package cz.jakubmaly.pxslt;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import java.io.*;
import java.util.ArrayList;

public class SlicedInput {

    public InputStream[] inputStreams;

    public SlicedInput(InputStream[] inputStreams) {
        this.inputStreams = inputStreams;
    }

    public Iterable<Source> getSources() {
        ArrayList<Source> sources = new ArrayList<Source>();
        for (InputStream inputStream : inputStreams) {
            File f = new File("");
            sources.add(new StreamSource(inputStream));
        }
        return sources;
    }

    public void debugPrint(String pattern) throws IOException {
        for (InputStream inputStream : inputStreams) {
            BufferedReader r = new BufferedReader(new InputStreamReader(inputStream));
            int offset = 0;
            String line;
            char[] buffer = new char[1000];
            int read;
            do {
                read = r.read(buffer, 0, 1000);
                line = String.valueOf(buffer);
                offset += line.length();
            } while (read != -1 && line != null && pattern != null && !line.contains(pattern));
            System.out.println(line);
            System.out.println(line.indexOf(pattern));
            r.close();
        }

    }
}
