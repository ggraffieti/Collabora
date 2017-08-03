package org.gammf.collabora.communication

import akka.actor.ActorRef
import com.newmotion.akka.rabbitmq.Channel

/**
  * Created by mperuzzi on 03/08/17.
  */
trait Message

case class StartMessage() extends Message

case class ChannelCreationMessage(connection: ActorRef, exchange: String, queue: String,
                                  routingKey: Option[String])

case class ChannelCreatedMessage(channel: Channel)

case class SubscribeMessage(channel: Channel, queue: String) extends Message
