package org.example;

import lu.uni.serval.commons.runner.utils.process.ClassLauncher;
import org.example.helpers.CapitalizeServer;
import org.example.helpers.InvertServer;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.Socket;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class ProxyServerTest {
    @Test
    void testSingleConnection() throws IOException, InterruptedException {
        final ClassLauncher proxyServerProcess = new ClassLauncher(ProxyServer.class);
        final ClassLauncher invertServerProcess = new ClassLauncher(InvertServer.class);

        proxyServerProcess.execute(false);
        invertServerProcess.execute(false);

        try(Socket socket = new Socket("localhost", 8085))
        {
            ProxyFrame.writePayload("localhost:8090", "test" + System.lineSeparator(), socket.getOutputStream());
            final ProxyFrame frame = ProxyFrame.read(socket.getInputStream());

            assertEquals("localhost:8090", new String(frame.getAddress()));
            assertEquals("Server: tset" + System.lineSeparator(), new String(frame.getPayload()));
            assertNull(frame.getError());
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

        try(Socket socket = new Socket("localhost", 8085))
        {
            ProxyFrame.writePayload("localhost:8090", "test1" + System.lineSeparator(), socket.getOutputStream());
            ProxyFrame.writePayload("localhost:8090", "test2" + System.lineSeparator(), socket.getOutputStream());
            ProxyFrame.writePayload("localhost:8090", "test3" + System.lineSeparator(), socket.getOutputStream());

            // The server might decide to put two frames in one, depending on the speed
            // so in this case we don't know how many frames will arrive but we know the
            // result of the payload which is the concatenation of all the messages.
            String payload = "";
            int lines = 0;
            while (lines < 3){
                final ProxyFrame frame = ProxyFrame.read(socket.getInputStream());
                assertEquals("localhost:8090", new String(frame.getAddress()));
                assertNull(frame.getError());

                payload += new String(frame.getPayload());
                lines = payload.split(System.lineSeparator()).length;
            }

            assertEquals(
                    "Server: 1tset" + System.lineSeparator()
                    + "Server: 2tset" + System.lineSeparator()
                    + "Server: 3tset" + System.lineSeparator()
                    , payload
            );

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

        try(Socket socket = new Socket("localhost", 8085))
        {
            ProxyFrame.writePayload("localhost:8090", "test" + System.lineSeparator(), socket.getOutputStream());
            ProxyFrame.writePayload("localhost:8091", "test" + System.lineSeparator(), socket.getOutputStream());

            // We do not know in which order the frame will come back.
            // Thus, wait for both of them then identify them by their address
            final Set<ProxyFrame> frames = new HashSet<>(2);
            frames.add(ProxyFrame.read(socket.getInputStream()));
            frames.add(ProxyFrame.read(socket.getInputStream()));

            final Optional<ProxyFrame> frame8090 = frames.stream()
                    .filter(f -> new String(f.getAddress()).equals("localhost:8090"))
                    .findFirst();

            assertTrue(frame8090.isPresent());
            assertEquals("Server: tset" + System.lineSeparator(), new String(frame8090.get().getPayload()));

            final Optional<ProxyFrame> frame8091 = frames.stream()
                    .filter(f -> new String(f.getAddress()).equals("localhost:8091"))
                    .findFirst();

            assertTrue(frame8091.isPresent());
            assertEquals("Server: TEST" + System.lineSeparator(), new String(frame8091.get().getPayload()));
        }
        finally {
            proxyServerProcess.kill();
            invertServerProcess.kill();
            capitalizeServerProcess.kill();
        }
    }

    @Test
    void testNonExistingDestination() throws IOException, InterruptedException {
        final ClassLauncher proxyServerProcess = new ClassLauncher(ProxyServer.class);

        proxyServerProcess.execute(false);

        try(Socket socket = new Socket("localhost", 8085))
        {
            ProxyFrame.writePayload("localhost:8090", "test" + System.lineSeparator(), socket.getOutputStream());
            final ProxyFrame frame = ProxyFrame.read(socket.getInputStream());

            assertEquals("localhost:8090", new String(frame.getAddress()));
            assertEquals(ProxyFrame.Error.REFUSED, frame.getError());
        }
        finally {
            proxyServerProcess.kill();
        }
    }
}
