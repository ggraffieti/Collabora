package org.gammf.collabora.yellowpages.messages

import org.gammf.collabora.yellowpages.ActorService.ActorService
import org.gammf.collabora.yellowpages.TopicElement.TopicElement
import org.gammf.collabora.yellowpages.util.Topic

/**
  * Represents a generic message about a yellow pages issue.
  */
trait YellowPagesMessage

/**
  * Represents a registration request from an actor.
  * Contains the topic to which the actor asks to be registered and the service that offers.
  * @param topic the topic to which the actor asks to be registered.
  * @param service the service offered by the actor.
  */
case class RegistrationMessage(topic: Topic[TopicElement], service: ActorService) extends YellowPagesMessage

/**
  * Represents a confirmation of actor registration in the yellow pages.
  */
case class RegistrationOKMessage() extends YellowPagesMessage

/**
  * Represents an error in the actor registration in the yellow pages.
  */
case class RegistrationErrorMessage() extends YellowPagesMessage