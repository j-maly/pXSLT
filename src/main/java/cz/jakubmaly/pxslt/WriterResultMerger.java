package cz.jakubmaly.pxslt;

import org.apache.commons.io.IOUtils;

import java.io.*;

public class WriterResultMerger extends ResultMerger {
    private Writer result;

    public WriterResultMerger(Writer result) {
        this.result = result;
    }

    @Override
    protected void append(File file) throws IOException {
        try (FileReader input = new FileReader(file)) {
            IOUtils.copyLarge(input, result);
        }
    }
}
