package org.example;

import java.io.*;
import java.net.*;
import java.util.HashMap;
import java.util.Map;

public class RequestHandler implements Runnable {
    private final Socket clientSocket;
    private final Map<Address, Socket> serverSockets = new HashMap<>();

    public RequestHandler(final Socket clientSocket){
        this.clientSocket = clientSocket;
    }

    @Override
    public void run() {
        try{
            clientSocket.setKeepAlive(true);

            while (true) {
                final DataInputStream reader = new DataInputStream(this.clientSocket.getInputStream());
                final Socket serverSocket = extractServerSocket(reader);
                final byte[] payload = extractPayload(reader);

                if(serverSocket != null){
                    sendPayload(serverSocket.getOutputStream(), payload);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Socket extractServerSocket(final DataInputStream reader) throws IOException {
        int offset = reader.readInt();
        byte[] buffer = new byte[offset];

        final int size = reader.read(buffer, 0, offset);
        if(size != offset){
            throw new SocketException("Read " + size + " bytes instead of " + offset);
        }

        final Address address = new Address(buffer);

        Socket serverSocket;
        synchronized (serverSockets){
            if(serverSockets.containsKey(address)){
                serverSocket = serverSockets.get(address);
            }
            else{
                try {
                    System.out.println("Creating new connection: " + address);
                    serverSocket = new Socket(address.host, address.port);
                    serverSocket.setKeepAlive(true);

                    serverSockets.put(address, serverSocket);
                    ProxyServer.startManagedThread(new ForwardHandler(serverSocket.getInputStream(), clientSocket.getOutputStream()));
                }
                catch (IOException e){
                    serverSockets.remove(address);
                    serverSocket = null;
                }
            }
        }

        return serverSocket;
    }

    private static byte[] extractPayload(final DataInputStream reader) throws IOException {
        int offset = reader.readInt();
        byte[] payload = new byte[offset];

        final int size = reader.read(payload, 0, offset);
        if(size != offset){
            throw new SocketException("Read " + size + " bytes instead of " + offset);
        }

        return payload;
    }

    private static void sendPayload(OutputStream outputStream, byte[] payload) throws IOException {
        outputStream.write(payload);
        outputStream.flush();
    }

    private static class Address{
        public final String name;
        public final String host;
        public final int port;

        public Address(byte[] name) throws SocketException {
            this.name = new String(name);
            final String[] particles = this.name.split(":");

            if(particles.length != 2){
                throw new SocketException("Invalid address: " + this.name);
            }

            this.host = particles[0];
            this.port = Integer.parseInt(particles[1]);
        }

        @Override
        public boolean equals(Object other) {
            if(other instanceof Address){
                return this.name.equals(((Address) other).name);
            }

            return false;
        }

        @Override
        public int hashCode() {
            return this.name.hashCode();
        }

        @Override
        public String toString() {
            return "[host: " + host + "; port: " + port + "]";
        }
    }
}
