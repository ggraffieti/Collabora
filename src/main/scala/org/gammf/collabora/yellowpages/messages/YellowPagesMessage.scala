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
  * @param actor the actor to be unregistered.
  * @param topic the actor topic.
  * @param service the actor service.
  */
case class UnregistrationRequestMessage(actor: ActorRef, topic: Topic[TopicElement], service: ActorService)
  extends YellowPagesMessage
