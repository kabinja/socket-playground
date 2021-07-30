package org.example.proxy.socks.constants;

public enum Misc {
    RESERVED((byte)0x00);

    private final byte code;

    static final Misc[] MISCS = new Misc[256];

    static {
        for(Misc c: values()){
            MISCS[c.getCode()] = c;
        }
    }

    Misc(byte code) {
        this.code = code;
    }

    public byte getCode() {
        return code;
    }

    public static Misc decode(byte value) {
        return MISCS[value];
    }
}
