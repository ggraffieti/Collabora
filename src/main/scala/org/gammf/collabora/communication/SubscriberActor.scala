package org.gammf.collabora.communication

import akka.actor._
import com.newmotion.akka.rabbitmq._
import Utils._
/**
  * Created by mperuzzi on 03/08/17.
  */
class SubscriberActor extends Actor {

  override def receive: Receive = {
    case SubscribeMessage(channel, queue) =>
      val consumer = new DefaultConsumer(channel) {
        override def handleDelivery(consumerTag: String, envelope: Envelope,
                                    properties: BasicProperties, body: Array[Byte]) {
          println("Received: " + fromBytes(body) + " with routing key " + envelope.getRoutingKey)
          channel.basicAck(envelope.getDeliveryTag, false)
        }
      }
      channel.basicConsume(queue, false, consumer)
      println("Subscribtion started!")
    case _ => println("Huh?")
  }

}

