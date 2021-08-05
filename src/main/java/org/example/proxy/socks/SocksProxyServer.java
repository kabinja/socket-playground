package org.example.proxy.socks;

import org.example.proxy.ThreadUtils;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SocksProxyServer {
    private final ExecutorService executor = Executors.newCachedThreadPool();
    private final ServerSocket clientSocket;

    private volatile boolean running = true;

    public static void main(String[] args) {
        try {
            SocksProxyServer proxyServer = new SocksProxyServer(8095);
            proxyServer.listen();
        } catch (IOException e) {
            System.err.printf(
                    "Failed to start server: [%s] %s%n",
                    e.getClass().getSimpleName(),
                    e.getMessage()
            );
        }
    }

    public SocksProxyServer(int port) throws IOException {
        this.clientSocket = new ServerSocket(port);
        executor.submit(new MessagingHandler(this));
        System.out.println("Waiting for client on port " + clientSocket.getLocalPort() + "...");
    }

    public void listen(){
        while(running){
            try {
                final Socket socket = clientSocket.accept();
                executor.submit(new RequestHandler(socket));
            } catch (SocketException e) {
                System.out.println("Server closed");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try {
            this.clientSocket.close();
        } catch (IOException e) {
            System.err.printf(
                    "Failed to close SOCKS socket: [%s] %s%n",
                    e.getClass().getSimpleName(),
                    e.getMessage()
            );
        }

        ThreadUtils.shutDown(executor);
    }

    public void stop(){
        running = false;
        try {
            this.clientSocket.close();
        } catch (IOException e) {
            //ignore
        }
    }
}