package org.example.proxy.socks.constants;

public enum Reply {
    SUCCEEDED((byte)0x00),
    GENERAL_SOCKET_SERVER_FAILURE((byte)0x01),
    CONNECTION_NOT_ALLOW_BY_RULESET((byte)0x02),
    NETWORK_UNREACHABLE((byte)0x03),
    HOST_UNREACHABLE((byte)0x04),
    CONNECTION_REFUSED((byte)0x05),
    TTL_EXPIRED((byte)0x06),
    COMMAND_NOT_SUPPORTED((byte)0x07),
    ADDRESS_TYPE_NOT_SUPPORTED((byte)0x08);

    private final byte code;

    static final Reply[] REPLIES = new Reply[256];

    static {
        for(Reply c: values()){
            REPLIES[c.getCode()] = c;
        }
    }

    Reply(byte code) {
        this.code = code;
    }

    public byte getCode() {
        return code;
    }

    public static Reply decode(byte value) {
        return REPLIES[value];
    }
}
