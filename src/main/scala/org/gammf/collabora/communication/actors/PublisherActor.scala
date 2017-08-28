package org.gammf.collabora.communication.actors

import akka.actor.Actor
import org.gammf.collabora.communication.messages.PublishMessage
import play.api.libs.json.Json

/**
  * @author Manuel Peruzzi
  * This is an actor that publish a message in a certain exchange through a rabbitMQ channel.
  */
class PublisherActor extends Actor {

  override def receive: Receive = {
    case PublishMessage(channel, exchange, routingKey, message) =>
      channel.basicPublish(exchange, routingKey.getOrElse(""), null, message.toString.getBytes("UTF-8"))
      println("[PublisherActor] Message published! " + Json.prettyPrint(message) +", exchange: " + exchange + ", routing key " + routingKey)
    case _ => println("[PublisherActor] Huh?")
  }
}
