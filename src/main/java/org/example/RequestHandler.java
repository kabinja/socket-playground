package org.example;

import java.io.*;
import java.net.*;

public class RequestHandler implements Runnable {
    private final Socket clientSocket;
    private Socket serverSocket;

    public RequestHandler(final Socket clientSocket){
        this.clientSocket = clientSocket;
    }

    @Override
    public void run() {
        try{
            serverSocket = new Socket("localhost", 8090);

            clientSocket.setKeepAlive(true);
            serverSocket.setKeepAlive(true);

            startForwarding(clientSocket.getInputStream(), serverSocket.getOutputStream());
            startForwarding(serverSocket.getInputStream(), clientSocket.getOutputStream());
        } catch (IOException e) {
            connectionBroken();
            e.printStackTrace();
        }
    }

    public synchronized void connectionBroken() {
        try {
            serverSocket.close();
        } catch (Exception e) {
            System.err.printf("Failed to close serverSocket Connection: [%s] %s",
                    e.getClass().getSimpleName(),
                    e.getMessage()
            );
        }
    }

    private void startForwarding(final InputStream inputStream, final OutputStream outputStream){
        ProxyServer.startManagedThread(new ForwardHandler(this, inputStream, outputStream));
    }
}
