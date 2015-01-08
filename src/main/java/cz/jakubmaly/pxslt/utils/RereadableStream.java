package cz.jakubmaly.pxslt.utils;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;

public class RereadableStream extends InputStream {

    private InputStream underlyingStream;

    public RereadableStream(InputStream underlyingStream) throws IOException {
        if (underlyingStream.markSupported()) {
            this.underlyingStream = underlyingStream;
            this.underlyingStream.mark(Integer.MAX_VALUE);
        } else {
            throw new IOException();
        }
    }

    @Override
    public void close() throws IOException {
        underlyingStream.reset();
    }

    public void closeUnderlying() throws IOException {
        IOUtils.closeQuietly(underlyingStream);
    }

    @Override
    public int read() throws IOException {
        return underlyingStream.read();
    }
}
