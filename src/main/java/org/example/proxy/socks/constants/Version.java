package org.example.proxy.socks.constants;

public enum Version {
    V4((byte)0x04),
    V5((byte)0x05);

    private final byte code;

    static final Version[] VERSIONS = new Version[256];

    static {
        for(Version c: values()){
            VERSIONS[c.getCode()] = c;
        }
    }

    Version(byte code) {
        this.code = code;
    }

    public byte getCode() {
        return code;
    }

    public static Version decode(byte value) {
        return VERSIONS[value];
    }
}
