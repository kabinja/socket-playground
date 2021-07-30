package org.example.proxy;

public class Convertor {
    private Convertor() {}

    public static byte[] toByteArray(int value){
        return new byte[] {
                (byte)((value >> 24) & 0xff),
                (byte)((value >> 16) & 0xff),
                (byte)((value >> 8) & 0xff),
                (byte)((value >> 0) & 0xff),
        };
    }

    public static byte[] toByteArray(short value){
        return new byte[] {
                (byte)((value >> 8) & 0xff),
                (byte)((value >> 0) & 0xff),
        };
    }

    public static int toInt(byte[] bytes){
        if(bytes.length != 4){
            throw new IllegalArgumentException("int must be encoded on 4 bytes, received " + bytes.length + " instead!");
        }

        return (0xff & bytes[0]) << 24
                | (0xff & bytes[1]) << 16
                | (0xff & bytes[2]) << 8
                | (0xff & bytes[3]) << 0;
    }

    public static short toShort(byte[] bytes){
        if(bytes.length != 2){
            throw new IllegalArgumentException("short must be encoded on 2 bytes, received " + bytes.length + " instead!");
        }

        return (short) ((0xff & bytes[0]) << 8 | (0xff & bytes[1]) << 0);
    }
}
