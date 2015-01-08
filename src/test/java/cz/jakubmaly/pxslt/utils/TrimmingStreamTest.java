package cz.jakubmaly.pxslt.utils;

import cz.jakubmaly.pxslt.StreamUtils;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;

import static org.assertj.core.api.Assertions.assertThat;

public class TrimmingStreamTest {

    @Test
    public void TestRead1() throws IOException {
        InputStream stream = TrimmingStreamTest.class.getResourceAsStream("/xml/books.xml");
        TrimmingStream tr = new TrimmingStream(stream, 57, 274);
        String s = StreamUtils.getString(tr);
        String control = "<Book>\r\n" +
                "        <Title>My Life and Times</Title>\r\n" +
                "        <Author>Paul McCartney</Author>\r\n" +
                "        <Date>1998</Date>\r\n" +
                "        <ISBN>1-56592-235-2</ISBN>\r\n" +
                "        <Publisher>McMillin Publishing</Publisher>\r\n" +
                "    </Book>";
        assertThat(s).isEqualTo(control);
    }

    @Test
    public void TestRead2() throws IOException {
        InputStream stream = TrimmingStreamTest.class.getResourceAsStream("/xml/books.xml");
        TrimmingStream tr = new TrimmingStream(stream, 280, 525);
        String s = StreamUtils.getString(tr);
        String control = "<Book>\r\n" +
                "        <Title>Illusions The Adventures of a Reluctant Messiah</Title>\r\n" +
                "        <Author>Richard Bach</Author>\r\n" +
                "        <Date>1977</Date>\r\n" +
                "        <ISBN>0-440-34319-4</ISBN>\r\n" +
                "        <Publisher>Dell Publishing Co.</Publisher>\r\n" +
                "    </Book>";
        assertThat(s).isEqualTo(control);
    }

    @Test
    public void TestRead3() throws IOException {
        InputStream stream = TrimmingStreamTest.class.getResourceAsStream("/xml/books.xml");
        TrimmingStream tr = new TrimmingStream(stream, 531, 755);
        String s = StreamUtils.getString(tr);
        String control = "<Book>\r\n" +
                "        <Title>The First and Last Freedom</Title>\r\n" +
                "        <Author>J. Krishnamurti</Author>\r\n" +
                "        <Date>1954</Date>\r\n" +
                "        <ISBN>0-06-064831-7</ISBN>\r\n" +
                "        <Publisher>Harper &amp; Row</Publisher>\r\n" +
                "    </Book>";
        assertThat(s).isEqualTo(control);
    }

    @Test
    public void TestBufferedRead() throws IOException {
        InputStream stream = TrimmingStreamTest.class.getResourceAsStream("/xml/books.xml");
        TrimmingStream tr = new TrimmingStream(stream, 531, 755);
        String s = StreamUtils.getStringWithBuffer(tr);
        String control = "<Book>\r\n" +
                "        <Title>The First and Last Freedom</Title>\r\n" +
                "        <Author>J. Krishnamurti</Author>\r\n" +
                "        <Date>1954</Date>\r\n" +
                "        <ISBN>0-06-064831-7</ISBN>\r\n" +
                "        <Publisher>Harper &amp; Row</Publisher>\r\n" +
                "    </Book>";
        assertThat(s).isEqualTo(control);
    }
}
