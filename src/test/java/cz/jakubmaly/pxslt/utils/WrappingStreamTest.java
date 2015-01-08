package cz.jakubmaly.pxslt.utils;

import cz.jakubmaly.pxslt.StreamUtils;
import org.apache.commons.io.IOUtils;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

import static org.assertj.core.api.Assertions.assertThat;

public class WrappingStreamTest {

    @Test
    public void TestRead1() throws IOException {
        InputStream stream = WrappingStream.class.getResourceAsStream("/xml/books.xml");
        TrimmingStream tr = new TrimmingStream(stream, 57, 274);
        String prefix = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><BookStore>";
        String suffix = "</BookStore>";
        WrappingStream w = new WrappingStream(tr, prefix, suffix);
        String s = StreamUtils.getString(w);
        String control = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><BookStore><Book>\r\n" +
                "        <Title>My Life and Times</Title>\r\n" +
                "        <Author>Paul McCartney</Author>\r\n" +
                "        <Date>1998</Date>\r\n" +
                "        <ISBN>1-56592-235-2</ISBN>\r\n" +
                "        <Publisher>McMillin Publishing</Publisher>\r\n" +
                "    </Book></BookStore>";
        System.out.println(s);
        IOUtils.closeQuietly(w);
        assertThat(s).isEqualTo(control);
    }

    String control = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><BookStore><Book>\r\n" +
            "        <Title>My Life and Times</Title>\r\n" +
            "        <Author>Paul McCartney</Author>\r\n" +
            "        <Date>1998</Date>\r\n" +
            "        <ISBN>1-56592-235-2</ISBN>\r\n" +
            "        <Publisher>McMillin Publishing</Publisher>\r\n" +
            "    </Book></BookStore>";

    @Test
    public void TestBufferedRead() throws IOException {
        InputStream stream = TrimmingStreamTest.class.getResourceAsStream("/xml/books.xml");
        TrimmingStream tr = new TrimmingStream(stream, 57, 274);
        String prefix = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><BookStore>";
        String suffix = "</BookStore>";
        WrappingStream w = new WrappingStream(tr, prefix, suffix);
        String result = StreamUtils.getStringWithBuffer(w);
        System.out.println(result);
        IOUtils.closeQuietly(w);
        assertThat(result).isEqualTo(control);
    }

    @Test
    public void TestBufferedRead2() throws IOException {
        InputStream stream = TrimmingStreamTest.class.getResourceAsStream("/xml/books.xml");
        TrimmingStream tr = new TrimmingStream(stream, 57, 274);
        String prefix = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><BookStore>";
        String suffix = "</BookStore>";
        WrappingStream w = new WrappingStream(tr, prefix, suffix);
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        byte[] buffer = new byte[11];
        IOUtils.copyLarge(w, os, buffer);
        String result = new String(os.toByteArray(), Charset.forName("UTF-8"));
        IOUtils.closeQuietly(w);
        assertThat(result).isEqualTo(control);
    }

    @Test
    public void TestBigFileRead() throws IOException {
        InputStream stream = TrimmingStreamTest.class.getResourceAsStream("/xml/books.xml");
        String prefix = "<?xml version=\"1.0\" encoding=\"utf-8\"?><ProteinDatabase>";
        String suffix = "</ProteinDatabase>";
        int[] bounds = {0, 181_598, 360_272, 542_031, -1};
        for (int i = 0; i < 4; i++) {
            TrimmingStream tr = new TrimmingStream(stream, bounds[i], bounds[i+1]);
            WrappingStream w = new WrappingStream(tr, prefix, suffix);
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            byte[] buffer = new byte[11];
            IOUtils.copyLarge(w, os, buffer);
            String result = new String(os.toByteArray(), Charset.forName("UTF-8"));
            System.out.println(i);
        }
        IOUtils.closeQuietly(stream);
    }
}
