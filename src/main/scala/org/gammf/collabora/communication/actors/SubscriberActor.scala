package org.gammf.collabora.communication.actors

import akka.actor._
import com.newmotion.akka.rabbitmq._
import org.gammf.collabora.communication.Utils.fromBytes
import org.gammf.collabora.communication.messages.{ClientUpdateMessage, SubscribeMessage}

/**
  * @author Manuel Peruzzi
  * This is an actor that subscribes on a certain queue in a rabbitMQ channel, capturing all the messages.
  */
class SubscriberActor extends Actor {

  private var messageSender: Option[ActorRef] = None

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
      println("[Subscriber Actor] Subscribtion started!")
    case _ => println("[Subscriber Actor] Huh?")
  }

}

