package org.gammf.collabora.communication

import akka.actor.Actor

/**
  * Created by mperuzzi on 03/08/17.
  */
class SubscriberActor extends Actor {

  override def receive = {
    case SubscribeMessage(channel, exchange, queue, routingKey) =>
      println("Subscribe message received!")
    case _ => println("Huh?")
  }

}
