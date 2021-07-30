package org.example.proxy.socks;

import org.example.proxy.socks.constants.Command;
import org.example.proxy.socks.constants.Reply;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

public class CommandProcessor {
    private CommandProcessor() {}

    public static Socket process(final OutputStream out, final Command command, final SocksSettings settings) throws IOException {
        switch (command){
            case CONNECT: return processConnect(out, settings);
            case BIND: return processBind(out, settings);
            case UDP_ASSOCIATE: return processUdpAssociate(out, settings);
        }

        throw new IllegalArgumentException("Command not supported: " + command);
    }

    private static Socket processConnect(final OutputStream out, final SocksSettings settings) throws IOException {
        final EndPoint endPoint = settings.getEndPoint();
        final Socket socket = new Socket(endPoint.getAddress(), endPoint.getPort());
        socket.setKeepAlive(true);
        socket.setTcpNoDelay(true);

        ResponseWriter.write(out, Reply.SUCCEEDED, settings);

        return socket;
    }

    private static Socket processBind(final OutputStream out, final SocksSettings settings){
        throw new NullPointerException("Not Implemented");
    }

    private static Socket processUdpAssociate(final OutputStream out, final SocksSettings settings){
        throw new NullPointerException("Not Implemented");
    }
}
