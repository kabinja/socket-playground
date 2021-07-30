package org.example.proxy.custom;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.HashSet;
import java.util.Set;

public class CustomProxyServer {
    private static final Set<Thread> servicingThreads = new HashSet<>();
    private ServerSocket clientSocket;

    public static void main(String[] args) {
        try {
            CustomProxyServer customProxyServer = new CustomProxyServer(8085);
            customProxyServer.listen();
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
        thread.start();

        synchronized(servicingThreads) {
            servicingThreads.add(thread);
        }
    }

    public CustomProxyServer(int port) throws IOException {
        this.clientSocket = new ServerSocket(port);
        System.out.println("Waiting for client on port " + clientSocket.getLocalPort() + "..");
    }

    public void listen(){
        while(true){
            try {
                final Socket socket = clientSocket.accept();
                System.out.println("Connection established");
                startManagedThread(new RequestHandler(socket));
            } catch (SocketException e) {
                System.out.println("Server closed");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void closeServer(){
        System.out.println("\nClosing Server..");

        try{
            for(Thread thread : servicingThreads){
                if(thread.isAlive()){
                    System.out.print("Waiting on "+  thread.getId()+" to close..");
                    thread.join();
                    System.out.println(" closed");
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        try{
            System.out.println("Terminating Connection");
            clientSocket.close();
        } catch (Exception e) {
            System.out.println("Exception closing proxy's server socket");
            e.printStackTrace();
        }

    }
}
