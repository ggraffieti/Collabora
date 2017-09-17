package org.gammf.collabora.communication.actors.rabbitmq

import akka.actor._
import com.newmotion.akka.rabbitmq._

import org.gammf.collabora.communication.messages.{ClientUpdateMessage, SubscribeMessage}
import org.gammf.collabora.communication.fromBytes
import org.gammf.collabora.yellowpages.ActorService._
import org.gammf.collabora.yellowpages.actors.BasicActor
import org.gammf.collabora.yellowpages.util.Topic.ActorTopic

/**
  * This is an actor that, when invoked by another actor, subscribes to a certain queue in a rabbitMQ channel, capturing
  * all the messages and forwarding them to the other actor.
  */
class RabbitMQSubscriberActor(override val yellowPages: ActorRef, override val name: String,
                              override val topic: ActorTopic, override val service: ActorService = Subscribing) extends BasicActor {

  override def receive: Receive = ({
    case SubscribeMessage(channel, queue) => subscribe(channel, queue, sender)
  }: Receive) orElse super[BasicActor].receive

  private[this] def subscribe(channel: Channel, queue: String, sender: ActorRef): Unit = {
    val consumer = new DefaultConsumer(channel) {
      override def handleDelivery(consumerTag: String, envelope: Envelope, properties: BasicProperties, body: Array[Byte]) {
        channel.basicAck(envelope.getDeliveryTag, false)
        sender ! ClientUpdateMessage(body)
      }
    }
    channel.basicConsume(queue, false , consumer)
    println("[Subscriber Actor] Subscribtion started!")
  }
}

object RabbitMQSubscriberActor{

  /**
    * Factory methods that return a [[Props]] to create a subscriber registered actor
    * @param yellowPages the reference to the yellow pages root actor.
    * @param topic the topic to which this actor is going to be registered.
    * @return the [[Props]] to use to create a subscriber actor.
    */

  def subscriberProps(yellowPages: ActorRef, topic: ActorTopic, name: String = "SubscriberActor") : Props =
    Props(new RabbitMQSubscriberActor(yellowPages = yellowPages, name = name, topic = topic))
}

