package org.gammf.collabora.communication.actors

import akka.actor._
import com.newmotion.akka.rabbitmq._
import org.gammf.collabora.communication.Utils.fromBytes
import org.gammf.collabora.communication.messages.{ClientUpdateMessage, SubscribeMessage}
import org.gammf.collabora.yellowpages.ActorService.{ActorService, Subscribing}
import org.gammf.collabora.yellowpages.actors.BasicActor
import org.gammf.collabora.yellowpages.util.Topic.ActorTopic

/**
  * This is an actor that subscribes on a certain queue in a rabbitMQ channel, capturing all the messages.
  */
class SubscriberActor(override val yellowPages: ActorRef,
                      override val name: String,
                      override val topic: ActorTopic,
                      override val service: ActorService = Subscribing) extends BasicActor {

  private[this] var messageSender: Option[ActorRef] = None

  override def receive: Receive = ({
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
  }: Receive) orElse super[BasicActor].receive
}

object SubscriberActor{

  /**
    * Factory methods that return a [[Props]] to create a subscriber registered actor
    * @param yellowPages the reference to the yellow pages root actor.
    * @param topic the topic to which this actor is going to be registered.
    * @return the [[Props]] to use to create a subscriber actor.
    */

  def printerProps(yellowPages: ActorRef, topic: ActorTopic, name: String = "SubscriberActor") : Props =
    Props(new SubscriberActor(yellowPages = yellowPages, name = name, topic = topic))
}

