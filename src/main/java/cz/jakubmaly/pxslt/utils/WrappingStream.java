package cz.jakubmaly.pxslt.utils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

public class WrappingStream extends InputStream {
    private InputStream underlyingStream;
    private String prefix;
    private String suffix;
    private byte[] prefixChars;
    private byte[] suffixChars;

    private ReadingState state;
    private int pos;

    public WrappingStream(InputStream underlyingStream, String prefix, String suffix) throws IOException {
        this.underlyingStream = underlyingStream;
        this.prefix = prefix;
        this.suffix = suffix;
        prefixChars = prefix == null ? new byte[0] : prefix.getBytes(Charset.forName("UTF-8"));
        suffixChars = suffix == null ? new byte[0] : suffix.getBytes(Charset.forName("UTF-8"));
        state = ReadingState.ReadingPrefix;
        pos = 0;
    }

    @Override
    public int read() throws IOException {
        switch (state) {
            case ReadingPrefix:
                if (prefixChars == null || pos >= prefixChars.length) {
                    state = ReadingState.ReadingStream;
                    return read();
                } else {
                    return prefixChars[pos++];
                }
            case ReadingStream:
                int read = underlyingStream.read();
                if (read == -1) {
                    pos = 0;
                    state = ReadingState.ReadingSuffix;
                    return read();
                }
                return read;
            case ReadingSuffix:
                if (suffixChars == null || pos >= suffixChars.length) {
                    return -1;
                } else {
                    return suffixChars[pos++];
                }
            default:
                return -1;
        }
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        if (state == ReadingState.Done)
            return -1;

        int read = 0;

        if (state == ReadingState.ReadingPrefix && len > 0) {
            if (prefixChars.length - pos > 0) {
                int prefixLen = Math.min(prefixChars.length - pos, len);
                System.arraycopy(prefixChars, pos, b, off, prefixLen);
                pos += prefixLen;
                off += prefixLen;
                read += prefixLen;
                len -= prefixLen;
            }
            if (prefixChars.length == 0 || pos == prefixChars.length) {
                state = ReadingState.ReadingStream;
            }
        }

        if (state == ReadingState.ReadingStream && len > 0) {
            int streamLen = underlyingStream.read(b, off, len);
            if (streamLen != -1) {
                read += streamLen;
                off += streamLen;
                len -= streamLen;
                pos += streamLen;
            }

            if (streamLen <= 0 || streamLen < len) {
                state = ReadingState.ReadingSuffix;
                pos = 0;
            }
        }

        if (state == ReadingState.ReadingSuffix && len > 0) {
            if (suffixChars.length - pos > 0) {
                int suffixLen = Math.min(suffixChars.length - pos, len);
                System.arraycopy(suffixChars, pos, b, off, suffixLen);
                pos += suffixLen;
                read += suffixLen;
            }
            if (suffixChars.length == 0 || pos == suffixChars.length) {
                state = ReadingState.Done;
            }
        }

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
        long skip = underlyingStream.skip(n);
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

    private enum ReadingState {
        ReadingPrefix,
        ReadingStream,
        ReadingSuffix,
        Done
    }
}


