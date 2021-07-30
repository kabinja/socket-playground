
package org.example.proxy.socks;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

class ForwardHandler implements Runnable {
    public enum Direction {
        CLIENT_TO_SERVER,
        SERVER_TO_CLIENT
    }

    private static final int BUFFER_SIZE = 8192;

    private final Direction direction;
    private final InputStream inputStream;
    private final OutputStream outputStream;
    private final RequestHandler requestHandler;

    public ForwardHandler(final Direction direction, final RequestHandler requestHandler, final InputStream inputStream, final OutputStream outputStream) {
        this.direction = direction;
        this.inputStream = inputStream;
        this.outputStream = outputStream;
        this.requestHandler = requestHandler;
    }

    @Override
    public void run() {
        byte[] buffer = new byte[BUFFER_SIZE];

        try {
            while (true) {
                int bytesRead = inputStream.read(buffer);
                if (bytesRead == -1) continue;

                switch (direction){
                    case CLIENT_TO_SERVER: interceptClientToServer();
                    case SERVER_TO_CLIENT: interceptServerToClient();
                }

                outputStream.write(buffer, 0, bytesRead);
                outputStream.flush();
            }
        } catch (IOException | InterruptedException e) {
            System.err.printf(
                    "Forwarding interupted: [%s] %s%n",
                    e.getClass().getSimpleName(),
                    e.getMessage()
            );
        }

        requestHandler.connectionBroken();
    }

    private static void interceptClientToServer() throws InterruptedException {
        System.out.println("Sleep for 100ms");
        Thread.sleep(100);
    }

    private static void interceptServerToClient() throws InterruptedException {
        System.out.println("Sleep for 200ms");
        Thread.sleep(200);
    }
}