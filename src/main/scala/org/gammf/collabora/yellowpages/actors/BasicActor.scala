package org.gammf.collabora.yellowpages.actors

import akka.actor.{Actor, ActorRef}
import org.gammf.collabora.yellowpages.ActorService.ActorService
import org.gammf.collabora.yellowpages.messages._
import org.gammf.collabora.yellowpages.util.Topic
import org.gammf.collabora.yellowpages.util.Topic.ActorTopic

/**
  * Represents a simple actor identified by a name, registered to a [[Topic]], offering an [[ActorService]].
  * In order to communicate with other actors, this actor needs a reference to a [[YellowPagesActor]].
  */
trait BasicActor extends Actor {
  /**
    * Returns a reference to the yellow pages root actor.
    */
  def yellowPages: ActorRef

  /**
    * Returns the name of this actor.
    */
  def name: String

  /**
    * Returns the topic to which this actor is registered.
    */
  def topic: ActorTopic

  /**
    * Returns the service offered by this actor.
    */
  def service: ActorService

  override def preStart(): Unit = super.preStart; yellowPages !
    RegistrationRequestMessage(reference = self, name = name, topic = topic, service = service)

  override def receive: Receive = {
    case RegistrationResponseMessage() => println("[" + name + "] Registration OK.")
    case InsertionErrorMessage() => println("[" + name + "] Insertion Error.")
    case _ => println("["+ name + "] Huh?"); unhandled(_)
  }
}
