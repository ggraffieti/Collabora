package org.gammf.collabora.communication

import akka.actor._
import com.newmotion.akka.rabbitmq._
import com.rabbitmq.client.BuiltinExchangeType

/**
  * Created by mperuzzi on 03/08/17.
  */
class ChannelCreatorActor extends Actor {

  var messageSender: Option[ActorRef] = None

  override def receive: Receive = {
    case SubscribingChannelCreationMessage(connection, exchange, queue, routingKey) =>
      createChannel(connection, exchange, Some(queue), routingKey)

    case PublishingChannelCreationMessage(connection, exchange) =>
      createChannel(connection, exchange, None, None)

    case ChannelCreated(_) => println("Channel created!")

    case _ => println("Huh?")
  }

  private def createChannel(connection: ActorRef, exchange: String,
                            queue: Option[String], routingKey: Option[String]) = {
    def setup(channel: Channel, self: ActorRef) {
      channel.exchangeDeclare(exchange, BuiltinExchangeType.DIRECT, true)
      queue match {
        case Some(q) =>
          channel.queueDeclare(q, true, false, false, null)
          channel.queueBind(q, exchange, routingKey.getOrElse(""))
          messageSender.get ! ChannelCreatedMessage(channel)
        case _ =>
      }
    }
    messageSender = Some(sender)
    connection ! CreateChannel(ChannelActor.props(setup))
  }

}
