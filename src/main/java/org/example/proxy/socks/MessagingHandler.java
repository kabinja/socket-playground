package org.example.proxy.socks;

import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.*;

public class MessagingHandler implements Runnable, ExceptionListener {
    private final SocksProxyServer server;

    public MessagingHandler(SocksProxyServer server) {
        this.server = server;
    }

    @Override
    public void run() {
        final ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory("tcp://localhost:61616");

        try {
            final Connection connection = connectionFactory.createConnection();
            connection.start();
            connection.setExceptionListener(this);

            final Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            final Destination destination = session.createQueue("SOCKS.PROXY.KILL");
            final MessageConsumer consumer = session.createConsumer(destination);
            final Message message = consumer.receive();

            if (message instanceof TextMessage) {
                TextMessage textMessage = (TextMessage) message;
                String text = textMessage.getText();
                System.out.println("Received: " + text);
                this.server.stop();
            }

            consumer.close();
            session.close();
            connection.close();
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onException(JMSException e) {
        System.out.println("JMS Exception occured.  Shutting down client.");
    }
}
