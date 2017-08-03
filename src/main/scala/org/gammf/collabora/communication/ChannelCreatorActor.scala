package org.gammf.collabora.communication

import akka.actor._
import com.newmotion.akka.rabbitmq._
import com.rabbitmq.client.BuiltinExchangeType

/**
  * Created by mperuzzi on 03/08/17.
  */
class ChannelCreatorActor extends Actor {

  override def receive: Receive = {
    case ChannelCreationMessage(connection, exchange, queue, routingKey) =>
      val subscribeName = exchange + "-" + queue + "-" + "subscribe"
      def setupSubscriber(channel: Channel, self: ActorRef) {
        channel.exchangeDeclare(exchange, BuiltinExchangeType.DIRECT, true)
        channel.queueDeclare(queue, true, false, false, null)
        channel.queueBind(queue, exchange, routingKey.getOrElse(""))
        sender ! channel
      }
      connection ! CreateChannel(ChannelActor.props(setupSubscriber), Some(subscribeName))
    case _ => println("Huh?")
  }

}
