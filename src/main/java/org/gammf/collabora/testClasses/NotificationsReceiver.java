package org.gammf.collabora.testClasses;

import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import com.rabbitmq.client.AMQP;

import java.io.IOException;

/**
 * Simple class that receives a notification message from the broker.
 */
public final class NotificationsReceiver {

    private static final String EXCHANGE_NAME = "notifications";
    private static final String ROUTING_KEY = "59806a4af27da3fcfe0ac0ca";
    private static final String BROKER_HOST = "localhost";

    private NotificationsReceiver() { }

    /**
     * Entry point of the test application.
     * @param args default java main parameters.
     * @throws Exception if something went wrong.
     */
    public static void main(final String[] args) throws Exception {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(BROKER_HOST);
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();

        channel.exchangeDeclare(EXCHANGE_NAME, BuiltinExchangeType.DIRECT, true);
        String queueName = channel.queueDeclare().getQueue();
        channel.queueBind(queueName, EXCHANGE_NAME, ROUTING_KEY);

        System.out.println(" [*] Waiting for messages. To exit press CTRL+C");

        Consumer consumer = new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(final String consumerTag, final Envelope envelope,
                                       final AMQP.BasicProperties properties, final byte[] body) throws IOException {
                String message = new String(body, "UTF-8");
                System.out.println(" [x] Received '" + message + "'");
            }
        };
        channel.basicConsume(queueName, true, consumer);
    }
}
