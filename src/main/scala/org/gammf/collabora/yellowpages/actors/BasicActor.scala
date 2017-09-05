package org.gammf.collabora.yellowpages.actors

import akka.actor.{Actor, ActorRef}
import org.gammf.collabora.yellowpages.ActorService.ActorService
import org.gammf.collabora.yellowpages.TopicElement.TopicElement
import org.gammf.collabora.yellowpages.messages._
import org.gammf.collabora.yellowpages.util.Topic

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
  def topic: Topic[TopicElement]

  /**
    * Returns the service offered by this actor.
    */
  def service: ActorService

  override def preStart(): Unit = super.preStart; yellowPages ! RegistrationRequestMessage(self, topic, service)

  override def receive: Receive = {
    case RegistrationResponseMessage() => println("[" + name + "] Registration OK.")
      // TODO remove this message from here and handle it in all the subclasses.
    case _ => println("["+ name + "] Huh?")
  }
}
