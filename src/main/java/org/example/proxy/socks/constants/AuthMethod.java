package org.example.proxy.socks.constants;

public enum AuthMethod {
    NO_AUTHENTICATION((byte)0x00),
    GSS_API((byte)0x01),
    USERNAME_AND_PASSWORD((byte)0x02);

    private final byte code;

    static final AuthMethod[] AUTH_METHODS = new AuthMethod[256];

    static {
        for(AuthMethod c: values()){
            AUTH_METHODS[c.getCode()] = c;
        }
    }

    AuthMethod(byte code) {
        this.code = code;
    }

    public byte getCode() {
        return code;
    }

    public static AuthMethod decode(byte value) {
        return AUTH_METHODS[value];
    }
}
