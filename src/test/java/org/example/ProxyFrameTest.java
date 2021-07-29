package org.example;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

import static org.junit.jupiter.api.Assertions.*;

class ProxyFrameTest {
    @Test
    void writeAndReadPayload() throws IOException {
        final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ProxyFrame.writePayload("localhost:8090", "test\n", outputStream);

        final ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());
        final ProxyFrame frame = ProxyFrame.read(inputStream);

        assertEquals("localhost:8090", new String(frame.getAddress()));
        assertEquals("test\n", new String(frame.getPayload()));
        assertNull(frame.getError());
    }

    @Test
    void writeAndReadError() throws IOException {
        final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ProxyFrame.writeError("localhost:8080", ProxyFrame.Error.BUSY, outputStream);

        final ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());
        final ProxyFrame frame = ProxyFrame.read(inputStream);

        assertEquals("localhost:8080", new String(frame.getAddress()));
        assertEquals(ProxyFrame.Error.BUSY, frame.getError());
        assertNull(frame.getPayload());
    }

    @Test
    void testByteArrayToInt(){
        assertEquals(10, ProxyFrame.toInt(intToBytes(10)));
        assertEquals(-1, ProxyFrame.toInt(intToBytes(-1)));
        assertEquals(0, ProxyFrame.toInt(intToBytes(0)));
        assertEquals(500, ProxyFrame.toInt(intToBytes(500)));
    }

    @Test
    void testIntToByteArray(){
        assertArrayEquals(intToBytes(10), ProxyFrame.toByteArray(10));
        assertArrayEquals(intToBytes(-1), ProxyFrame.toByteArray(-1));
        assertArrayEquals(intToBytes(0), ProxyFrame.toByteArray(0));
        assertArrayEquals(intToBytes(500), ProxyFrame.toByteArray(500));
    }

    private static byte[] intToBytes( final int i ) {
        ByteBuffer bb = ByteBuffer.allocate(4);
        bb.putInt(i);
        return bb.array();
    }
}