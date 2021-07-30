package org.example.proxy.custom;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;

class ForwardHandler implements Runnable {
    private static final int BUFFER_SIZE = 8192;

    private final byte[] address;
    private final InputStream inputStream;
    private final OutputStream outputStream;

    public ForwardHandler(byte[] address, InputStream inputStream, OutputStream outputStream) {
        this.address = address;
        this.inputStream = inputStream;
        this.outputStream = outputStream;
    }

    @Override
    public void run() {
        byte[] buffer = new byte[BUFFER_SIZE];

        try {
            while (true){
                int bytesRead = inputStream.read(buffer);
                if (bytesRead != -1){
                    ProxyFrame.writePayload(address, Arrays.copyOfRange(buffer, 0, bytesRead), outputStream);
                    System.out.println("[From server to client] " + new String(Arrays.copyOfRange(buffer, 0, bytesRead)));
                }
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
