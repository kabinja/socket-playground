package org.example;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class ProxyFrame {
    private final byte[][] frame;
    private final byte[] address;
    private final byte[] payload;
    private final Error error;

    private ProxyFrame(byte[] address, byte[] payload){
        if(payload != null){
            this.frame = new byte[][]{
                    Section.ADDRESS.getCode(),
                    toByteArray(address.length),
                    address,
                    Section.PAYLOAD.getCode(),
                    toByteArray(payload.length),
                    payload
            };

            this.address = address;
            this.payload = payload;
            this.error = null;
        }
        else {
            throw new NullPointerException("Either payload or error must be non null");
        }
    }

    private ProxyFrame(byte[] address, Error error){
        this.frame = new byte[][]{
                Section.ADDRESS.getCode(),
                toByteArray(address.length),
                address,
                Section.ERROR.getCode(),
                error.getCode()
        };

        this.address = address;
        this.payload = null;
        this.error = error;
    }

    public byte[] getAddress() {
        return address;
    }

    public byte[] getPayload() {
        return payload;
    }

    public Error getError() {
        return error;
    }

    public static void writePayload(String address, String payload, OutputStream outputStream) throws IOException {
        writePayload(address.getBytes(StandardCharsets.UTF_8), payload.getBytes(StandardCharsets.UTF_8), outputStream);
    }

    public static void writePayload(byte[] address, byte[] payload, OutputStream outputStream) throws IOException {
        final ProxyFrame frame = new ProxyFrame(address, payload);
        write(frame, outputStream);
    }

    public static void writeError(String address, Error error, OutputStream outputStream) throws IOException {
        writeError(address.getBytes(StandardCharsets.UTF_8), error, outputStream);
    }

    public static void writeError(byte[] address, Error error, OutputStream outputStream) throws IOException {
        final ProxyFrame frame = new ProxyFrame(address, error);
        write(frame, outputStream);
    }

    public static void write(ProxyFrame frame, OutputStream outputStream) throws IOException {
        for(byte[] section: frame.frame){
            outputStream.write(section);
        }

        outputStream.flush();
    }

    public static ProxyFrame read(InputStream inputStream) throws IOException {
        if(readSection(inputStream) != Section.ADDRESS){
            throw new IOException("ProxyFrame must start with an address section!");
        }

        byte[] address = inputStream.readNBytes(readLength(inputStream));

        Section section = readSection(inputStream);

        if(section == Section.PAYLOAD){
            byte[] payload = inputStream.readNBytes(readLength(inputStream));
            return new ProxyFrame(address, payload);
        }
        else if(section == Section.ERROR){
            Error error = readError(inputStream);
            return new ProxyFrame(address, error);
        }

        throw new IOException("Invalid frame, expecting a payload or error section");
    }

    private static Section readSection(InputStream inputStream) throws IOException {
        return Section.decode(inputStream.readNBytes(1));
    }

    private static Error readError(InputStream inputStream) throws IOException {
        return Error.decode(inputStream.readNBytes(1));
    }

    private static int readLength(InputStream inputStream) throws IOException {
        int length = toInt(inputStream.readNBytes(4));

        if(length < 1){
            throw new IOException("Length must be strictly greater than 0!");
        }

        return length;
    }

    public static byte[] toByteArray(int value){
        return new byte[] {
                (byte)((value >> 24) & 0xff),
                (byte)((value >> 16) & 0xff),
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

    public enum Section {
        ADDRESS('a'), PAYLOAD('p'), ERROR('e');

        private final byte[] code;

        static final Section[] SECTIONS = new Section[256];

        static {
            for(Section c: values())
                SECTIONS[c.getCode()[0]] = c;
        }

        Section(char code) {
            this.code = new byte[] { (byte) code };
        }

        public byte[] getCode() {
            return code;
        }

        public static Section decode(byte[] bytes) {
            return SECTIONS[bytes[0]];
        }
    }

    public enum Error {
        TIMEOUT('t'), REFUSED('r'), BUSY('b');

        private final byte[] code;

        static final Error[] ERRORS = new Error[256];

        static {
            for(Error c: values())
                ERRORS[c.getCode()[0]] = c;
        }

        Error(char code) {
            this.code = new byte[] { (byte) code };
        }

        public byte[] getCode() {
            return code;
        }

        public static Error decode(byte[] bytes) {
            return ERRORS[bytes[0]];
        }
    }
}
