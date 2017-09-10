package org.gammf.collabora.yellowpages.util

import akka.actor.ActorRef
import org.gammf.collabora.yellowpages.ActorService.ActorService
import org.gammf.collabora.yellowpages.util.Topic.ActorTopic

trait ActorInformation {
  def reference: ActorRef
  def topic: ActorTopic
  def service: ActorService
}

case class ActorInformationImpl(override val reference: ActorRef, override val topic: ActorTopic,
                                override val service: ActorService) extends ActorInformation

object ActorInformation {
  def apply(reference: ActorRef, topic: ActorTopic, service: ActorService): ActorInformation =
    ActorInformationImpl(reference, topic, service)
}
