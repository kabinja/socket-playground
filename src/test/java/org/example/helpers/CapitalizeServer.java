package org.example.helpers;

public class CapitalizeServer extends Server {
    protected CapitalizeServer(int port) {
        super(port);
    }

    public static void main(String[] args) {
        Server server = new CapitalizeServer(8091);
        server.listen();
    }

    @Override
    String transform(String payload) {
        return payload.toUpperCase();
    }
}
