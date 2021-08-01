package org.example.proxy.socks;

import org.example.proxy.Convertor;
import org.example.proxy.ThreadUtils;
import org.example.proxy.socks.constants.*;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

class RequestHandler implements Runnable {
    private final Socket clientSocket;
    private final SocksSettings settings = new SocksSettings();
    private final ExecutorService toServerExecutor = Executors.newSingleThreadExecutor();
    private final ExecutorService toClientExecutor = Executors.newSingleThreadExecutor();

    private Socket serverSocket;

    public RequestHandler(final Socket clientSocket){
        this.clientSocket = clientSocket;
    }

    @Override
    public void run() {
        try{
            clientSocket.setKeepAlive(true);
            clientSocket.setTcpNoDelay(true);

            serverSocket = establishConnection();
            System.out.printf("Connection Established with %s:%d%n", serverSocket.getInetAddress().getHostAddress(), serverSocket.getPort());

            toServerExecutor.submit(new ForwardHandler(
                    ForwardHandler.Direction.CLIENT_TO_SERVER,
                    this,
                    clientSocket.getInputStream(),
                    serverSocket.getOutputStream()
            ));

            toClientExecutor.submit(new ForwardHandler(
                    ForwardHandler.Direction.SERVER_TO_CLIENT,
                    this,
                    serverSocket.getInputStream(),
                    clientSocket.getOutputStream()
            ));

        } catch (final UnknownHostException e) {
            try {
                ResponseWriter.write(
                        clientSocket.getOutputStream(),
                        Reply.HOST_UNREACHABLE,
                        settings
                );
            } catch (final IOException ignore) {}
        }catch (ConnectException e) {
            if(e.getMessage().contains("Connection refused")){
                try {
                    ResponseWriter.write(
                            clientSocket.getOutputStream(),
                            Reply.CONNECTION_REFUSED,
                            settings
                    );
                } catch (final IOException ignore) {}
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Socket establishConnection() throws IOException {
        final InputStream in = clientSocket.getInputStream();
        final OutputStream out = clientSocket.getOutputStream();

        negotiateAuthMethod(in, out);
        return createConnection(in, out);
    }

    private void negotiateAuthMethod(final InputStream in, final OutputStream out) throws IOException {
        byte version = in.readNBytes(1)[0];
        settings.setVersion(Version.decode(version));

        byte numberMethods = in.readNBytes(1)[0];
        byte[] methods = in.readNBytes(numberMethods);

        if(!containsNoAuth(methods)){
            throw new SocketException("This server only supports No Auth exception, but not offered by the client");
        }

        settings.setAuthMethod(AuthMethod.NO_AUTHENTICATION);
        out.write(new byte[]{settings.getVersion().getCode(), settings.getAuthMethod().getCode()});
    }

    private Socket createConnection(final InputStream in, final OutputStream out) throws IOException {
        final byte version = in.readNBytes(1)[0];

        if(version != settings.getVersion().getCode()){
            throw new SocketException("Unexpected version received: got " + version + " but expected " + settings.getVersion());
        }

        final Command command = Command.decode(in.readNBytes(1)[0]);

        in.readNBytes(1);

        InetAddress address;
        byte[] addressBuffer;

        final AddressType addressType = AddressType.decode(in.readNBytes(1)[0]);
        switch (addressType){
            case IP_V4:
                addressBuffer = in.readNBytes(4);
                address = InetAddress.getByAddress(addressBuffer);
                break;

            case DOMAIN:
                addressBuffer = in.readNBytes(16);
                address =InetAddress.getByAddress(addressBuffer);
                break;

            case IP_V6:
                byte sizeBuffer = in.readNBytes(1)[0];
                byte[] nameBuffer = in.readNBytes(sizeBuffer & 0xFF);

                addressBuffer = new byte[1 + sizeBuffer];
                addressBuffer[0] = sizeBuffer;
                System.arraycopy(nameBuffer, 0, addressBuffer, 1, sizeBuffer);

                String name = new String(nameBuffer, StandardCharsets.US_ASCII);
                address =InetAddress.getByName(name);
                break;

            default:
                throw new IllegalStateException("Unexpected Address Type: " + addressType);
        }

        int port = Convertor.toShort(in.readNBytes(2)) & 0xFFFF;

        final EndPoint endPoint = new EndPoint(addressType, addressBuffer, address, port);
        settings.setEndPoint(endPoint);

        return CommandProcessor.process(out, command, settings);
    }

    private boolean containsNoAuth(byte[] methods){
        for(byte method: methods){
            if(method == AuthMethod.NO_AUTHENTICATION.getCode()){
                return true;
            }
        }

        return false;
    }

    public synchronized void connectionBroken() {
        try {
            if(serverSocket != null){
                serverSocket.close();
            }

            ThreadUtils.shutDown(toClientExecutor);
            ThreadUtils.shutDown(toServerExecutor);

        } catch (Exception e) {
            System.err.printf("Failed to close serverSocket Connection: [%s] %s",
                    e.getClass().getSimpleName(),
                    e.getMessage()
            );
        }
    }
}