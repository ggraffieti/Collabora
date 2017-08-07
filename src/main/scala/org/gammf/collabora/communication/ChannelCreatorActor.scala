package org.gammf.collabora.communication

import akka.actor._
import com.newmotion.akka.rabbitmq._
import com.rabbitmq.client.BuiltinExchangeType

/**
  * @author Manuel Peruzzi
  * This is an actor that builds and returns to the sender a specific RabbitMQ channel created on the provided connection.
  */
class ChannelCreatorActor extends Actor {

  private var messageSender: Option[ActorRef] = None

  override def receive: Receive = {
    case SubscribingChannelCreationMessage(connection, exchange, queue, routingKey) =>
      createChannel(connection, exchange, Some(queue), routingKey)

    case PublishingChannelCreationMessage(connection, exchange, routingKey) =>
      createChannel(connection, exchange, None, routingKey)

    case ChannelCreated(_) => println("[Channel Creator Actor] Channel created!")

    case _ => println("[Channel Creator Actor] Huh?")
  }

  private def createChannel(connection: ActorRef, exchange: String,
                            queue: Option[String], routingKey: Option[String]) = {
    def setup(channel: Channel, self: ActorRef) {
      channel.exchangeDeclare(exchange, BuiltinExchangeType.DIRECT, true)
      queue match {
        case Some(q) =>
          channel.queueDeclare(q, true, false, false, null)
          channel.queueBind(q, exchange, routingKey.getOrElse(""))
        case _ =>
      }
      messageSender.get ! ChannelCreatedMessage(channel)
    }
    messageSender = Some(sender)
    connection ! CreateChannel(ChannelActor.props(setup))
  }

}
