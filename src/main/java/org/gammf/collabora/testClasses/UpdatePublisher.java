package org.gammf.collabora.testClasses;

import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

/**
 * Simple class that sends a message to the broker, simulating a message of note creation from a mobile device.
 */
public final class UpdatePublisher {

    private static final String EXCHANGE_NAME = "updates";
    private static final String ROUTING_KEY = "";
    private static final String BROKER_HOST = "localhost";

    private UpdatePublisher() { }

    /**
     * Entry point of the test application.
     * @param args default java main parameters.
     * @throws Exception if something went wrong.
     */
    public static void main(final String[] args) throws Exception {
        final ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(BROKER_HOST);
        final Connection connection = factory.newConnection();
        final Channel channel = connection.createChannel();
        channel.exchangeDeclare(EXCHANGE_NAME, BuiltinExchangeType.DIRECT, true);

        final String message = "{\"messageType\": \"CREATION\",\"collaborationId\":\"59806a4af27da3fcfe0ac0ca\",\"target\" : \"NOTE\",\"user\" : \"maffone\",\"note\": {\"content\" : \"note\",\"expiration\" : \"2017-08-07T08:01:17.171+02:00\",\"location\" : { \"latitude\" : 546, \"longitude\" : 324 },\"state\" : { \"definition\" : \"Done\", \"responsible\" : \"maffone\"}}}";
        channel.basicPublish(EXCHANGE_NAME, ROUTING_KEY, null, message.getBytes("UTF-8"));
        System.out.println("[x] Sent: " + message);

        channel.close();
        connection.close();
    }
}
