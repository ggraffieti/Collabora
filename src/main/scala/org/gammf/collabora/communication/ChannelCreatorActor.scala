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
    case ChannelCreationMessage(connection, exchange, queue, routingKey) =>
      def setupSubscriber(channel: Channel, self: ActorRef) {
        channel.exchangeDeclare(exchange, BuiltinExchangeType.DIRECT, true)
        channel.queueDeclare(queue, true, false, false, null)
        channel.queueBind(queue, exchange, routingKey.getOrElse(""))
        messageSender.get ! ChannelCreatedMessage(channel)
      }
      messageSender = Some(sender)
      connection ! CreateChannel(ChannelActor.props(setupSubscriber))
    case ChannelCreated(_) => println("Channel created!")
    case _ => println("Huh?")
  }

}
