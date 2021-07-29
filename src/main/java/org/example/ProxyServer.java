package org.example;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

public class ProxyServer implements Runnable{
    private static final Set<Thread> servicingThreads = new HashSet<>();

    private ServerSocket clientSocket;
    private volatile boolean running = true;

    public static void main(String[] args) {
        ProxyServer proxyServer = new ProxyServer(8085);
        proxyServer.listen();
    }

    public static void startManagedThread(Runnable runnable){
        Thread thread = new Thread(runnable);
        thread.start();

        synchronized(servicingThreads) {
            servicingThreads.add(thread);
        }
    }

    public ProxyServer(int port) {
        try {
            this.clientSocket = new ServerSocket(port);
            this.running = true;

            System.out.println("Waiting for client on port " + clientSocket.getLocalPort() + "..");
        }
        catch (IOException io) {
            System.out.println("IO exception when connecting to client");
        }
    }

    public void listen(){
        while(running){
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
        running = false;

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

    @Override
    public void run() {
        try(Scanner scanner = new Scanner(System.in)){
            while(running){
                System.out.println("Type \"shutdown\" to close server.");
                final String command = scanner.nextLine();

                if(command.equals("shutdown")){
                    running = false;
                    closeServer();
                }
            }
        }
    }

}
