package org.gammf.collabora.testClasses;

import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.Channel;

/**
 * Simple class that sends a message to the broker, simulating a message of note creation from a mobile device.
 */
public final class EmitLog {

  private static final String EXCHANGE_NAME = "updates";

  private EmitLog() { }

    /**
     * Entry point of the application.
     * @param argv default java main parameters
     * @throws Exception if somethings went wrong
     */
  public static void main(final String[] argv) throws Exception {
    ConnectionFactory factory = new ConnectionFactory();
    factory.setHost("localhost");
    Connection connection = factory.newConnection();
    Channel channel = connection.createChannel();
    channel.exchangeDeclare(EXCHANGE_NAME, BuiltinExchangeType.DIRECT, true);

    String message = "{\"messageType\": \"insertion\",\"target\" : \"note\",\"user\" : \"maffone\",\"note\": {\"content\" : \"setup working enviroment\",\"expiration\" : \"2017-08-07T06:01:17.171Z\",\"location\" : { \"latitude\" : 546, \"longitude\" : 324 },\"previousNotes\" : [ \"5980710df27da3fcfe0ac88e\", \"5980710df27da3fcfe0ac88f\" ],\"state\" : { \"definition\" : \"done\", \"username\" : \"maffone\"}}}";
    
    channel.basicPublish(EXCHANGE_NAME, "", null, message.getBytes("UTF-8"));
    System.out.println(" [x] Sent '" + message + "'");

    channel.close();
    connection.close();
  }

  private static String getMessage(final String[] strings) {
    if (strings.length < 1) {
      return "info: Hello World!";
    }
    return joinStrings(strings, " ");
  }

  private static String joinStrings(final String[] strings, final  String delimiter) {
    int length = strings.length;
    if (length == 0) {
        return "";
    }
    StringBuilder words = new StringBuilder(strings[0]);
    for (int i = 1; i < length; i++) {
        words.append(delimiter).append(strings[i]);
    }
    return words.toString();
  }
}
