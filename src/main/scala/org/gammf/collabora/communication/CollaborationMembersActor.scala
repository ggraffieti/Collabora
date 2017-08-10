package org.gammf.collabora.communication

import akka.actor.{Actor, ActorRef}

/**
  * @author Manuel Peruzzi
  */

/**
  * This is an actor that sends all the information needed by a user that has just been added to a collaboration.
  * @param connection the open connection with the rabbitMQ broker.
  * @param naming the reference to a rabbitMQ naming actor.
  * @param channelCreator the reference to a channel creator actor.
  * @param publisher the reference to a publisher actor.
  */
class CollaborationMembersActor(connection: ActorRef, naming: ActorRef, channelCreator: ActorRef,
                                publisher: ActorRef) extends Actor {

  override def receive: Receive = {
    case _ => println("[UserFinderActor] Huh?")
  }

}
