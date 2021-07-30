package org.example.proxy.socks.constants;

public enum AddressType {
    IP_V4((byte) 0x01),
    DOMAIN((byte) 0x03),
    IP_V6((byte) 0x04);

    private final byte code;

    static final AddressType[] ADDRESS_TYPES = new AddressType[256];

    static {
        for(AddressType c: values()){
            ADDRESS_TYPES[c.getCode()] = c;
        }
    }

    AddressType(byte code) {
        this.code = code;
    }

    public byte getCode() {
        return code;
    }

    public static AddressType decode(byte value) {
        return ADDRESS_TYPES[value];
    }
}
