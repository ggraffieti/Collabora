package org.gammf.collabora.communication.messages

import akka.actor.ActorRef
import com.newmotion.akka.rabbitmq.Channel

/**
  * @author Manuel Peruzzi
  */

/**
  * Simple trait that represents a message about a rabbitMQ channel.
  */
trait ChannelMessage extends Message

/**
  * Represents a subscribing channel building request.
  * @param connection the open connection with the rabbitMQ broker.
  * @param exchange the name of the exchange to be declared.
  * @param queue the name of the queue to be declared.
  * @param routingKey the routing key that can be used in the exchange-queue binding.
  */
case class SubscribingChannelCreationMessage(connection: ActorRef, exchange: String, queue: String,
                                             routingKey: Option[String]) extends ChannelMessage

/**
  * Represents a publishing channel building request.
  * @param connection the open connection with the rabbitMQ broker.
  * @param exchange the name of the exchange to be declared.
  */
case class PublishingChannelCreationMessage(connection: ActorRef, exchange: String,
                                            routingKey: Option[String]) extends ChannelMessage

/**
  * Represents a response to a channel building request.
  * @param channel the channel created.
  */
case class ChannelCreatedMessage(channel: Channel) extends ChannelMessage

