package org.gammf.collabora.communication

import akka.actor.ActorRef
import com.newmotion.akka.rabbitmq.Channel
import play.api.libs.json.JsValue

/**
  * @author Manuel Peruzzi
  */

/**
  * Simple trait that represents a message between actors.
  */
trait Message

/**
  * Simple message used to start an actor.
  */
case class StartMessage() extends Message

/**
  * Represents a request for retrieve the rabbitMQ terminology names about a certain communication.
  * @param communicationType the type of the communication.
  */
case class ChannelNamesRequestMessage(communicationType: CommunicationType.Value) extends Message

/**
  * Represents a response to a rabbitMQ terminology names request.
  * @param exchange the name of the exchange to be used.
  * @param queue the name of the queue, only in case of subscribing channel.
  */
case class ChannelNamesResponseMessage(exchange: String, queue: Option[String]) extends Message

/**
  * Represents a subscribing channel building request.
  * @param connection the open connection with the rabbitMQ broker.
  * @param exchange the name of the exchange to be declared.
  * @param queue the name of the queue to be declared.
  * @param routingKey the routing key that can be used in the exchange-queue binding.
  */
case class SubscribingChannelCreationMessage(connection: ActorRef, exchange: String, queue: String,
                                  routingKey: Option[String]) extends Message

/**
  * Represents a publishing channel building request.
  * @param connection the open connection with the rabbitMQ broker.
  * @param exchange the name of the exchange to be declared.
  */
case class PublishingChannelCreationMessage(connection: ActorRef, exchange: String,
                                           routingKey: Option[String]) extends Message

/**
  * Represents a response to a channel building request.
  * @param channel the channel created.
  */
case class ChannelCreatedMessage(channel: Channel) extends Message

/**
  * Contains the information needed to make a subscription on the provided queue.
  * @param channel the rabbitMQ channel.
  * @param queue the subscription queue.
  */
case class SubscribeMessage(channel: Channel, queue: String) extends Message

/**
  * Contains the information needed by a publisher to publish a message on the rabbitMQ broker.
  * @param channel the rabbitMQ channel.
  * @param exchange the rabbitMQ exchange to use for the pubblication.
  * @param routingKey the routing key to be used to identify the concerned queues.
  * @param message the message to be published.
  */
case class PublishMessage(channel: Channel, exchange: String, routingKey: Option[String],
                          message: String) extends Message

/**
  * Contains the update message sent by a client.
  * @param text the text of the update.
  */
case class ClientUpdateMessage(text: String) extends Message

/**
  * Represents a notification message to be published.
  * @param collaborationID the identifier of the collaboration to which the message is addressed.
  * @param message the text of the message to be published in json format.
  */
case class PublishNotificationMessage(collaborationID: String, message: JsValue) extends Message

/**
  * Represents a message sent to a user that has just been added to a collaboration.
  * @param username the identifier of the user to which the message is addressed.
  * @param message the text of the message to be published in json format.
  */
case class PublishMemberAddedMessage(username: String, message: JsValue) extends Message
