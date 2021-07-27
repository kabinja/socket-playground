package org.example;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class ForwardHandler implements Runnable {
    private static final int BUFFER_SIZE = 8192;

    private final InputStream inputStream;
    private final OutputStream outputStream;
    private final RequestHandler requestHandler;

    public ForwardHandler(RequestHandler requestHandler, InputStream inputStream, OutputStream outputStream) {
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
                if (bytesRead == -1) break;
                outputStream.write(buffer, 0, bytesRead);
                outputStream.flush();
            }
        } catch (IOException e) {
            System.err.printf(
                    "Forwarding interupted: [%s] %s%n",
                    e.getClass().getSimpleName(),
                    e.getMessage()
            );
        }

        requestHandler.connectionBroken();
    }
}
