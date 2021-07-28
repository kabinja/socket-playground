package org.example.utils;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public abstract class Server {
    private final int port;

    protected Server(int port) {
        this.port = port;
    }

    void listen(){
        try (ServerSocket serverSocket = new ServerSocket(port)) {

            System.out.println("Server is listening on port " + port);

            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("New client connected");

                InputStream input = socket.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(input));

                OutputStream output = socket.getOutputStream();
                PrintWriter writer = new PrintWriter(output, true);


                String text;
                while((text = reader.readLine()) != null) {
                    System.out.println("Received: " + text);
                    writer.println("Server: " + transform(text));
                }

                socket.close();
            }

        } catch (IOException ex) {
            System.out.println("Server exception: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    abstract String transform(String payload);
}
