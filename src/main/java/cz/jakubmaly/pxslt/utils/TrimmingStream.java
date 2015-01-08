package cz.jakubmaly.pxslt.utils;

import java.io.IOException;
import java.io.InputStream;

public class TrimmingStream extends InputStream {

    private InputStream underlyingStream;
    private int startPos = 0;
    private int endPos = -1;
    private int pos;

    public TrimmingStream(InputStream underlyingStream, int startPos, int endPos) throws IOException {
        this.underlyingStream = underlyingStream;
        this.startPos = startPos;
        if (this.startPos != -1) {
            this.underlyingStream.skip(this.startPos);
            this.pos = this.startPos;
        }
        this.endPos = endPos;
    }

    @Override
    public int read() throws IOException {
        if (endPos != -1 && pos >= endPos)
            return -1;
        int read = underlyingStream.read();
        pos++;
        return read;
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        if (endPos != -1)
            len = Math.min(len, endPos - pos);
        if (len <= 0)
            return -1;
        int read = underlyingStream.read(b, off, len);
        pos += read;
        return read;
    }

    @Override
    public boolean markSupported() {
        return underlyingStream.markSupported();
    }

    @Override
    public int available() throws IOException {
        return underlyingStream.available();
    }

    @Override
    public long skip(long n) throws IOException {
        if (endPos != -1)
            n = Math.min(n, endPos - pos);
        long skip = underlyingStream.skip(n);
        pos += skip;
        return skip;
    }

    @Override
    public void close() throws IOException {
        underlyingStream.close();
    }

    @Override
    public void mark(int readlimit) {
        underlyingStream.mark(readlimit);
    }

    @Override
    public void reset() throws IOException {
        underlyingStream.reset();
    }
}
