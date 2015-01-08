package cz.jakubmaly.pxslt;

import java.io.*;
import java.nio.channels.FileChannel;

public class FileStreamResultMerger extends ResultMerger {

    private FileOutputStream result;

    public FileStreamResultMerger(FileOutputStream result) {
        this.result = result;
    }

    @Override
    protected void append(File file) throws IOException {
        try (FileInputStream input = new FileInputStream(file)) {
            FileChannel c1 = input.getChannel();
            FileChannel c2 = result.getChannel();
            c2.transferFrom(c1, c2.size(), c1.size());
        }
    }
}
