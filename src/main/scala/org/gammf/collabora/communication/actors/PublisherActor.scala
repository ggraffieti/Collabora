package org.gammf.collabora.communication.actors

import akka.actor.ActorRef
import org.gammf.collabora.communication.messages.PublishMessage
import org.gammf.collabora.yellowpages.ActorService.ActorService
import org.gammf.collabora.yellowpages.actors.BasicActor
import org.gammf.collabora.yellowpages.util.Topic.ActorTopic
import play.api.libs.json.Json

/**
  * @author Manuel Peruzzi
  * This is an actor that publish a message in a certain exchange through a rabbitMQ channel.
  */
class PublisherActor(override val yellowPages: ActorRef, override val name: String,
                     override val topic: ActorTopic, override val service: ActorService) extends BasicActor {

  override def receive: Receive = ({
    case PublishMessage(channel, exchange, routingKey, message) =>
      channel.basicPublish(exchange, routingKey.getOrElse(""), null, message.toString.getBytes("UTF-8"))
      println("[PublisherActor] Message published! " + Json.prettyPrint(message) +", exchange: " + exchange + ", routing key " + routingKey)
  } :Receive) orElse super[BasicActor].receive
}
