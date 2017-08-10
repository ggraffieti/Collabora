package org.gammf.collabora.communication

import akka.actor.Actor

/**
  * @author Manuel Peruzzi
  * This is an actor that publish a message in a certain exchange through a rabbitMQ channel.
  */
class PublisherActor extends Actor {

  override def receive: Receive = {
    case PublishMessage(channel, exchange, routingKey, message) =>
      channel.basicPublish(exchange, routingKey.getOrElse(""), null, message.getBytes("UTF-8"))
      println("[PublisherActor] Message published!")
    case _ => println("[PublisherActor] Huh?")
  }

}