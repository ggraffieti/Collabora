package org.gammf.collabora.communication

import akka.actor.ActorRef
import com.newmotion.akka.rabbitmq.Channel

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
  * @param routingKey the routing key that can be used in the exchange-queue binding.
  */
case class ChannelNamesResponseMessage(exchange: String, queue: Option[String],
                                       routingKey: Option[String]) extends Message

/**
  * Represents a subscribing channel building request.
  * @param connection the open connection with the rabbitMQ broker
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
  * Contains the update message sent by a client.
  * @param text the text of the update.
  */
case class ClientUpdateMessage(text: String) extends Message
