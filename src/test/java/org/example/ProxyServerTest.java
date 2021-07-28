package org.example;

import lu.uni.serval.commons.runner.utils.process.ClassLauncher;
import org.example.utils.CapitalizeServer;
import org.example.utils.InvertServer;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

class ProxyServerTest {
    @Test
    void testSingleConnection() throws IOException, InterruptedException {
        final ClassLauncher proxyServerProcess = new ClassLauncher(ProxyServer.class);
        final ClassLauncher invertServerProcess = new ClassLauncher(InvertServer.class);

        proxyServerProcess.execute(false);
        invertServerProcess.execute(false);

        try(
                Socket socket = new Socket("localhost", 8085);
                DataOutputStream out = new DataOutputStream(socket.getOutputStream());
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))
        )
        {
            sendFrame("localhost:8090", out, "test\n");
            assertEquals("Server: tset", in.readLine());
        }
        finally {
            proxyServerProcess.kill();
            invertServerProcess.kill();
        }
    }

    @Test
    void testSingleConnectionMultipleFrames() throws IOException, InterruptedException {
        final ClassLauncher proxyServerProcess = new ClassLauncher(ProxyServer.class);
        final ClassLauncher invertServerProcess = new ClassLauncher(InvertServer.class);

        proxyServerProcess.execute(false);
        invertServerProcess.execute(false);

        try(
                Socket socket = new Socket("localhost", 8085);
                DataOutputStream out = new DataOutputStream(socket.getOutputStream());
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))
        )
        {
            sendFrame("localhost:8090", out, "test1\n");
            sendFrame("localhost:8090", out, "test2\n");
            sendFrame("localhost:8090", out, "test3\n");

            assertEquals("Server: 1tset", in.readLine());
            assertEquals("Server: 2tset", in.readLine());
            assertEquals("Server: 3tset", in.readLine());
        }
        finally {
            proxyServerProcess.kill();
            invertServerProcess.kill();
        }
    }

    @Test
    void testMultipleConnectionMultipleFrames() throws IOException, InterruptedException {
        final ClassLauncher proxyServerProcess = new ClassLauncher(ProxyServer.class);
        final ClassLauncher invertServerProcess = new ClassLauncher(InvertServer.class);
        final ClassLauncher capitalizeServerProcess = new ClassLauncher(CapitalizeServer.class);

        proxyServerProcess.execute(false);
        invertServerProcess.execute(false);
        capitalizeServerProcess.execute(false);

        try(
                Socket socket = new Socket("localhost", 8085);
                DataOutputStream out = new DataOutputStream(socket.getOutputStream());
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))
        )
        {
            sendFrame("localhost:8090", out, "test1\n");
            sendFrame("localhost:8090", out, "test2\n");
            sendFrame("localhost:8091", out, "test3\n");

            assertEquals("Server: 1tset", in.readLine());
            assertEquals("Server: 2tset", in.readLine());
            assertEquals("Server: TEST3", in.readLine());
        }
        finally {
            proxyServerProcess.kill();
            invertServerProcess.kill();
            capitalizeServerProcess.kill();
        }
    }

    private static void sendFrame(String destination, DataOutputStream out, String message) throws IOException {
        final byte[] destinationBytes = destination.getBytes(StandardCharsets.UTF_8);
        final byte[] payloadBytes = message.getBytes(StandardCharsets.UTF_8);

        out.writeInt(destinationBytes.length);
        out.write(destinationBytes);
        out.writeInt(payloadBytes.length);
        out.write(payloadBytes);
        out.flush();
    }
}
