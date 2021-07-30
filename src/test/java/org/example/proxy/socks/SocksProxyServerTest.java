package org.example.proxy.socks;

import lu.uni.serval.commons.runner.utils.process.ClassLauncher;
import org.example.helpers.InvertServer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

class SocksProxyServerTest {
    @Test
    void testSingleConnection() throws IOException, InterruptedException {
        //final ClassLauncher proxyServerProcess = new ClassLauncher(SocksProxyServer.class);
        //final ClassLauncher invertServerProcess = new ClassLauncher(InvertServer.class);

        //proxyServerProcess.execute(false);
        //invertServerProcess.execute(false);

        try(
                Socket socket = getSocket("localhost", 8090);
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))
        )
        {

            assertTimeout(Duration.ofSeconds(10), () -> {
                final String payload = "test" + System.lineSeparator();
                final byte[] buffer = payload.getBytes(StandardCharsets.UTF_8);

                socket.getOutputStream().write(buffer);
                socket.getOutputStream().flush();
                assertEquals("Server: tset", in.readLine());
            });
        }
        finally {
            //proxyServerProcess.kill();
            //invertServerProcess.kill();
        }
    }

    protected Socket getSocket(String host, int port) throws IOException {
        final Proxy proxy = new Proxy(Proxy.Type.SOCKS, new InetSocketAddress("127.0.0.1", 8095));
        final Socket socket = new Socket(proxy);
        final InetSocketAddress socketHost = new InetSocketAddress(host, port);

        socket.connect(socketHost);

        return socket;
    }
}