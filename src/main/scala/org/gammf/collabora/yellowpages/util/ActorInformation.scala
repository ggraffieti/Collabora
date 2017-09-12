package org.gammf.collabora.yellowpages.util

import akka.actor.ActorRef
import org.gammf.collabora.yellowpages.ActorService.ActorService
import org.gammf.collabora.yellowpages.util.Topic.ActorTopic

/**
  * This trait gives an abstract representation of an actor inside the yellow pages system.
  */
trait ActorInformation {
  /**
    * The actor unique reference
    */
  def reference: ActorRef

  /**
    * The topic to which the actor is subscribed to
    */
  def topic: ActorTopic

  /**
    * The service offered by the actor
    */
  def service: ActorService
}

case class ActorInformationImpl(override val reference: ActorRef, override val topic: ActorTopic,
                                override val service: ActorService) extends ActorInformation

object ActorInformation {
  def apply(reference: ActorRef, topic: ActorTopic, service: ActorService): ActorInformation =
    ActorInformationImpl(reference, topic, service)
}
