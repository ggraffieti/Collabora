package org.gammf.collabora.communication.actors

import akka.actor._
import com.newmotion.akka.rabbitmq._
import com.rabbitmq.client.BuiltinExchangeType
import org.gammf.collabora.communication.messages.{ChannelCreatedMessage, PublishingChannelCreationMessage, SubscribingChannelCreationMessage}

/**
  * @author Manuel Peruzzi
  * This is an actor that builds and returns to the sender a specific RabbitMQ channel created on the provided connection.
  */
class ChannelCreatorActor extends Actor {

  override def receive: Receive = {
    case SubscribingChannelCreationMessage(connection, exchange, queue, routingKey) =>
      createChannel(connection, exchange, Some(queue), routingKey, sender)

    case PublishingChannelCreationMessage(connection, exchange, routingKey) =>
      createChannel(connection, exchange, None, routingKey, sender)

    case ChannelCreated(_) => println("[Channel Creator Actor] Channel created!")

    case _ => println("[Channel Creator Actor] Huh?")
  }

  private def createChannel(connection: ActorRef, exchange: String,
                            queue: Option[String], routingKey: Option[String], messageSender: ActorRef) = {
    def setup(channel: Channel, self: ActorRef) {
      channel.exchangeDeclare(exchange, BuiltinExchangeType.DIRECT, true)
      queue match {
        case Some(q) =>
          channel.queueDeclare(q, true, false, false, null)
          channel.queueBind(q, exchange, routingKey.getOrElse(""))
        case _ =>
      }
      messageSender ! ChannelCreatedMessage(channel)
    }
    connection ! CreateChannel(ChannelActor.props(setup))
  }

}
