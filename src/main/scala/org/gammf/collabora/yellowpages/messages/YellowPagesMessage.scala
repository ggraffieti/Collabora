package org.gammf.collabora.yellowpages.messages

import akka.actor.ActorRef
import org.gammf.collabora.yellowpages.ActorService.ActorService
import org.gammf.collabora.yellowpages.TopicElement.TopicElement
import org.gammf.collabora.yellowpages.util.Topic

/**
  * Represents a generic message about a yellow pages issue.
  */
trait YellowPagesMessage

/**
  * Represents a request about a deletion of a yellow pages entries.
  * @param reference the reference of the actor to be unregistered.
  * @param name the name of the actor to be unregistered.
  * @param topic the actor topic.
  * @param service the actor service.
  */
case class DeletionRequestMessage(reference: ActorRef, name: String, topic: Topic[TopicElement], service: ActorService)
  extends YellowPagesMessage
