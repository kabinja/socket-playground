package org.example.proxy.custom;

import org.example.proxy.Convertor;
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
}