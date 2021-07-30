package org.example.proxy;

import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;

import static org.junit.jupiter.api.Assertions.*;

class ConvertorTest {
    @Test
    void testByteArrayToInt(){
        assertEquals(10, Convertor.toInt(intToBytes(10)));
        assertEquals(-1, Convertor.toInt(intToBytes(-1)));
        assertEquals(0, Convertor.toInt(intToBytes(0)));
        assertEquals(500, Convertor.toInt(intToBytes(500)));
    }

    @Test
    void testIntToByteArray(){
        assertArrayEquals(intToBytes(10), Convertor.toByteArray(10));
        assertArrayEquals(intToBytes(-1), Convertor.toByteArray(-1));
        assertArrayEquals(intToBytes(0), Convertor.toByteArray(0));
        assertArrayEquals(intToBytes(500), Convertor.toByteArray(500));
    }

    @Test
    void testByteArrayToShort(){
        assertEquals(10, Convertor.toShort(shortToBytes((short)10)));
        assertEquals(-1, Convertor.toShort(shortToBytes((short)-1)));
        assertEquals(0, Convertor.toShort(shortToBytes((short)0)));
        assertEquals(500, Convertor.toShort(shortToBytes((short)500)));
    }

    private static byte[] intToBytes( final int i ) {
        ByteBuffer bb = ByteBuffer.allocate(4);
        bb.putInt(i);
        return bb.array();
    }

    private static byte[] shortToBytes(final short i) {
        ByteBuffer bb = ByteBuffer.allocate(2);
        bb.putShort(i);
        return bb.array();
    }
}