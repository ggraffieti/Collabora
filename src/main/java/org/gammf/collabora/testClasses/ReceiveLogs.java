package org.gammf.collabora.testClasses;

import com.rabbitmq.client.*;

import java.io.IOException;

/**
 * Simple java class that receives notifications from server.
 */
public final class ReceiveLogs {
  private static final String EXCHANGE_NAME = "notifications";

  private ReceiveLogs() { }

    /**
     * Entry point.
     * @param argv parameters in java main.
     * @throws Exception if something went wrong
     */
  public static void main(final String[] argv) throws Exception {
    ConnectionFactory factory = new ConnectionFactory();
    factory.setHost("localhost");
    Connection connection = factory.newConnection();
    Channel channel = connection.createChannel();

    channel.exchangeDeclare(EXCHANGE_NAME, BuiltinExchangeType.DIRECT, true);
    String queueName = channel.queueDeclare().getQueue();
    channel.queueBind(queueName, EXCHANGE_NAME, "maffone");

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
