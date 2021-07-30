package org.example.proxy.custom;

import java.io.*;
import java.net.*;
import java.util.HashMap;
import java.util.Map;

class RequestHandler implements Runnable {
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
                final ProxyFrame proxyFrame = ProxyFrame.read(this.clientSocket.getInputStream());

                try{
                    final Socket serverSocket = getServerSocket(proxyFrame);
                    final byte[] payload = proxyFrame.getPayload();
                    sendPayload(serverSocket.getOutputStream(), payload);
                }
                catch (ConnectException e){
                    if(e.getMessage().contains("Connection refused: connect")){
                        ProxyFrame.writeError(proxyFrame.getAddress(), ProxyFrame.Error.REFUSED, clientSocket.getOutputStream());
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Socket getServerSocket(final ProxyFrame proxyFrame) throws IOException {
        final Address address = new Address(proxyFrame.getAddress());

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
                    CustomProxyServer.startManagedThread(new ForwardHandler(proxyFrame.getAddress(), serverSocket.getInputStream(), clientSocket.getOutputStream()));
                }
                catch (IOException e){
                    serverSockets.remove(address);
                    throw e;
                }
            }
        }

        return serverSocket;
    }

    private static void sendPayload(OutputStream outputStream, byte[] payload) throws IOException {
        outputStream.write(payload);
        outputStream.flush();
        System.out.println("[From client to server] " + new String(payload));
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
