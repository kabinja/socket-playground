package org.example.proxy.socks;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.HashSet;
import java.util.Set;

public class SocksProxyServer {
    private static final Set<Thread> servicingThreads = new HashSet<>();

    private ServerSocket clientSocket;

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

    public static void startManagedThread(Runnable runnable){
        Thread thread = new Thread(runnable);
        servicingThreads.add(thread);
        thread.start();
    }

    public SocksProxyServer(int port) throws IOException {
        this.clientSocket = new ServerSocket(port);
        System.out.println("Waiting for client on port " + clientSocket.getLocalPort() + "..");
    }

    public void listen(){
        while(true){
            try {
                final Socket socket = clientSocket.accept();
                startManagedThread(new RequestHandler(socket));
            } catch (SocketException e) {
                System.out.println("Server closed");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}