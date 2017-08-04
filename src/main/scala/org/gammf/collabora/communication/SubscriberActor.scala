package org.gammf.collabora.communication

import akka.actor._
import com.newmotion.akka.rabbitmq._
import Utils._
/**
  * Created by mperuzzi on 03/08/17.
  */
class SubscriberActor extends Actor {

  var messageSender: Option[ActorRef] = None

  override def receive: Receive = {
    case SubscribeMessage(channel, queue) =>
      messageSender = Some(sender)
      val consumer = new DefaultConsumer(channel) {
        override def handleDelivery(consumerTag: String, envelope: Envelope,
                                    properties: BasicProperties, body: Array[Byte]) {
          channel.basicAck(envelope.getDeliveryTag, false)
          messageSender.get ! ClientUpdateMessage(fromBytes(body))
        }
      }
      channel.basicConsume(queue, false, consumer)
      println("Subscribtion started!")
    case _ => println("Huh?")
  }

}

