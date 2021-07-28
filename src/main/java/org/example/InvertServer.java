package org.example;

public class InvertServer extends Server {
    protected InvertServer(int port) {
        super(port);
    }

    public static void main(String[] args) {
        Server server = new InvertServer(8090);
        server.listen();
    }

    @Override
    String transform(String payload) {
        return new StringBuilder(payload).reverse().toString();
    }
}
