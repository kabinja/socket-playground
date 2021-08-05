package org.example.proxy.socks;

import lu.uni.serval.commons.runner.utils.messaging.Broker;
import lu.uni.serval.commons.runner.utils.process.BrokerLauncher;
import lu.uni.serval.commons.runner.utils.process.ClassLauncher;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.example.helpers.CapitalizeServer;
import org.example.helpers.InvertServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javax.jms.*;
import java.io.*;
import java.net.*;

import static org.junit.jupiter.api.Assertions.*;

class SocksProxyServerTest {
    private static BrokerLauncher brokerLauncher;

    @BeforeAll
    static void startBroker() throws IOException, InterruptedException {
        final String bindAddress = "tcp://localhost:61616";
        final String name = "testBroker";

        brokerLauncher = new BrokerLauncher(name, bindAddress);
        brokerLauncher.launchAndWaitForReady();
    }

    @AfterAll
    static void killBroker() {
        brokerLauncher.close();
    }

    @Test
    void testCloseProxy() throws JMSException, IOException, InterruptedException {
        final ClassLauncher proxyServerProcess = new ClassLauncher(SocksProxyServer.class);
        proxyServerProcess.execute(false);

        sendStopMessage();
        Thread.sleep(200);
        assertFalse(proxyServerProcess.isRunning());
    }

    @Test
    void testSingleConnection() throws IOException, InterruptedException {
        final ClassLauncher proxyServerProcess = new ClassLauncher(SocksProxyServer.class);
        final ClassLauncher invertServerProcess = new ClassLauncher(InvertServer.class);

        proxyServerProcess.execute(false);
        invertServerProcess.execute(false);

        try(
                Socket socket = getSocket("localhost", 8090);
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                DataOutputStream writer = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
        )
        {
             writer.writeBytes("test" + System.lineSeparator());
             writer.flush();

             assertEquals("Server: tset", in.readLine());
        }
        finally {
            proxyServerProcess.kill();
            invertServerProcess.kill();
        }
    }

    @Test
    void testSingleConnectionMultipleFrames() throws IOException, InterruptedException {
        final ClassLauncher proxyServerProcess = new ClassLauncher(SocksProxyServer.class);
        final ClassLauncher invertServerProcess = new ClassLauncher(InvertServer.class);

        proxyServerProcess.execute(false);
        invertServerProcess.execute(false);

        try(
                Socket socket = getSocket("localhost", 8090);
                DataOutputStream writer = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
                BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()))
        )
        {
            writer.writeBytes("test1" + System.lineSeparator());
            writer.writeBytes("test2" + System.lineSeparator());
            writer.writeBytes("test3" + System.lineSeparator());
            writer.flush();

            assertEquals("Server: 1tset", reader.readLine());
            assertEquals("Server: 2tset", reader.readLine());
            assertEquals("Server: 3tset", reader.readLine());

        }
        finally {
            proxyServerProcess.kill();
            invertServerProcess.kill();
        }
    }

    @Test
    void testMultipleConnectionMultipleFrames() throws IOException, InterruptedException {
        final ClassLauncher proxyServerProcess = new ClassLauncher(SocksProxyServer.class);
        final ClassLauncher invertServerProcess = new ClassLauncher(InvertServer.class);
        final ClassLauncher capitalizeServerProcess = new ClassLauncher(CapitalizeServer.class);

        proxyServerProcess.execute(false);
        invertServerProcess.execute(false);
        capitalizeServerProcess.execute(false);

        try(
                Socket socket8090 = getSocket("localhost", 8090);
                DataOutputStream writer8090 = new DataOutputStream(new BufferedOutputStream(socket8090.getOutputStream()));
                BufferedReader reader8090 = new BufferedReader(new InputStreamReader(socket8090.getInputStream()));

                Socket socket8091 = getSocket("localhost", 8091);
                DataOutputStream writer8091 = new DataOutputStream(new BufferedOutputStream(socket8091.getOutputStream()));
                BufferedReader reader8091 = new BufferedReader(new InputStreamReader(socket8091.getInputStream()));
        )
        {
            writer8090.writeBytes("invert1" + System.lineSeparator());
            writer8090.flush();
            writer8091.writeBytes("capitalize1"  + System.lineSeparator());
            writer8091.flush();
            writer8090.writeBytes("invert2" + System.lineSeparator());
            writer8090.flush();
            writer8091.writeBytes("capitalize2"  + System.lineSeparator());
            writer8091.flush();

            assertEquals("Server: 1trevni", reader8090.readLine());
            assertEquals("Server: 2trevni", reader8090.readLine());

            assertEquals("Server: CAPITALIZE1", reader8091.readLine());
            assertEquals("Server: CAPITALIZE2", reader8091.readLine());

        }
        finally {
            proxyServerProcess.kill();
            invertServerProcess.kill();
            capitalizeServerProcess.kill();
        }
    }

    @Test
    void testNonExistingDestination() throws IOException, InterruptedException {
        final ClassLauncher proxyServerProcess = new ClassLauncher(SocksProxyServer.class);
        proxyServerProcess.execute(false);

        try {
            assertThrows(IOException.class, () -> getSocket("localhost", 8091));
        }
        finally {
            proxyServerProcess.kill();
        }

    }

    private void sendStopMessage() throws JMSException {
        // Create a ConnectionFactory
        final ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory("tcp://localhost:61616");

        // Create a Connection
        final Connection connection = connectionFactory.createConnection();
        connection.start();

        // Create a Session
        final Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

        // Create the destination (Topic or Queue)
        final Destination destination = session.createQueue("SOCKS.PROXY.KILL");

        // Create a MessageProducer from the Session to the Topic or Queue
        final MessageProducer producer = session.createProducer(destination);
        producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);

        // Send message
        final TextMessage message = session.createTextMessage("STOP");
        producer.send(message);

        // Clean up
        session.close();
        connection.close();
    }

    private Socket getSocket(String host, int port) throws IOException {
        final Proxy proxy = new Proxy(Proxy.Type.SOCKS, new InetSocketAddress("127.0.0.1", 8095));
        final Socket socket = new Socket(proxy);
        final InetSocketAddress socketHost = new InetSocketAddress(host, port);

        socket.connect(socketHost);

        return socket;
    }
}