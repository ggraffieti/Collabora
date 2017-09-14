package org.gammf.collabora.communication.actors.rabbitmq

import akka.actor._
import com.newmotion.akka.rabbitmq._
import org.gammf.collabora.communication.messages.{ClientUpdateMessage, SubscribeMessage}
import org.gammf.collabora.communication.fromBytes
import org.gammf.collabora.yellowpages.ActorService.ActorService
import org.gammf.collabora.yellowpages.actors.BasicActor
import org.gammf.collabora.yellowpages.util.Topic.ActorTopic

/**
  * This is an actor that, when invoked by another actor, subscribes to a certain queue in a rabbitMQ channel, capturing
  * all the messages and forwarding them to the other actor.
  */
class RabbitMQSubscriberActor(override val yellowPages: ActorRef, override val name: String,
                              override val topic: ActorTopic, override val service: ActorService) extends BasicActor {

  private[this] var messageSender: Option[ActorRef] = None

  override def receive: Receive = ({
    case SubscribeMessage(channel, queue) =>
      messageSender = Some(sender)
      val consumer = new DefaultConsumer(channel) {
        override def handleDelivery(consumerTag: String, envelope: Envelope, properties: BasicProperties, body: Array[Byte]) {
          channel.basicAck(envelope.getDeliveryTag, false)
          messageSender.get ! ClientUpdateMessage(body)
        }
      }
      channel.basicConsume(queue, false, consumer)
      println("[Subscriber Actor] Subscribtion started!")
  }: Receive) orElse super[BasicActor].receive
}

