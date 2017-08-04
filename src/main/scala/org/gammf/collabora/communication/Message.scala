package org.gammf.collabora.communication

import akka.actor.ActorRef
import com.newmotion.akka.rabbitmq.Channel

/**
  * Created by mperuzzi on 03/08/17.
  */
trait Message

case class StartMessage() extends Message

case class ChannelNamesRequestMessage(communicationType: CommunicationType.Value)

case class ChannelNamesResponseMessage(exchange: String, queue: Option[String],
                                       routingKey: Option[String])

case class SubscribingChannelCreationMessage(connection: ActorRef, exchange: String, queue: String,
                                  routingKey: Option[String]) extends Message

case class PublishingChannelCreationMessage(connection: ActorRef, exchange: String) extends Message

case class ChannelCreatedMessage(channel: Channel) extends Message

case class SubscribeMessage(channel: Channel, queue: String) extends Message
