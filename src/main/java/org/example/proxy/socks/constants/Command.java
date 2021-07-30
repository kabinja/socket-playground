package org.example.proxy.socks.constants;

public enum Command {
    CONNECT((byte)0x01),
    BIND((byte)0x02),
    UDP_ASSOCIATE((byte)0x03);

    private final byte code;

    static final Command[] COMMANDS = new Command[256];

    static {
        for(Command c: values()){
            COMMANDS[c.getCode()] = c;
        }
    }

    Command(byte code) {
        this.code = code;
    }

    public byte getCode() {
        return code;
    }

    public static Command decode(byte value) {
        return COMMANDS[value];
    }
}
