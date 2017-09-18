package org.gammf.collabora.communication.actors.rabbitmq

import org.gammf.collabora.communication.toBytes

import akka.actor.{ActorRef, Props}
import org.gammf.collabora.communication.messages.PublishMessage
import org.gammf.collabora.yellowpages.ActorService.{ActorService, Publishing}
import org.gammf.collabora.yellowpages.actors.BasicActor
import org.gammf.collabora.yellowpages.util.Topic.ActorTopic
import play.api.libs.json.Json

/**
  * This is an actor that publish a message in a certain exchange through a rabbitMQ channel.
  */
class RabbitMQPublisherActor(override val yellowPages: ActorRef, override val name: String,
                             override val topic: ActorTopic, override val service: ActorService = Publishing) extends BasicActor {

  override def receive: Receive = ({
    case PublishMessage(channel, exchange, routingKey, message) =>
      channel.basicPublish(exchange, routingKey.getOrElse(""), null, message.toString)
      println("[" + name + "] Message published! " + Json.prettyPrint(message) +", exchange: " + exchange + ", routing key " + routingKey)
  } :Receive) orElse super[BasicActor].receive
}

object RabbitMQPublisherActor{

  /**
    * Factory method that returns a Props to create an already-registered publisher actor.
    * @param yellowPages the reference to the yellow pages root actor.
    * @param topic the topic to which this actor is going to be registered.
    * @return the Props to use to create a publisher actor.
    */

  def publisherProps(yellowPages: ActorRef, topic: ActorTopic, name:String = "PublisherActor") : Props =
    Props(new RabbitMQPublisherActor(yellowPages = yellowPages, name = name, topic = topic))

}
