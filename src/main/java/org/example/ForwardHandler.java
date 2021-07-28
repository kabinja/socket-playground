package org.example;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;

public class ForwardHandler implements Runnable {
    private static final int BUFFER_SIZE = 8192;

    private final InputStream inputStream;
    private final OutputStream outputStream;

    public ForwardHandler(InputStream inputStream, OutputStream outputStream) {
        this.inputStream = inputStream;
        this.outputStream = outputStream;
    }

    @Override
    public void run() {
        byte[] buffer = new byte[BUFFER_SIZE];

        try {
                int bytesRead = inputStream.read(buffer);
                if (bytesRead != -1){
                    outputStream.write(buffer, 0, bytesRead);
                    outputStream.flush();
                    System.out.println("Sent message: " + new String(Arrays.copyOfRange(buffer, 0, bytesRead)));
                }

        } catch (IOException e) {
            System.err.printf(
                    "Forwarding interupted: [%s] %s%n",
                    e.getClass().getSimpleName(),
                    e.getMessage()
            );
        }
    }
}
