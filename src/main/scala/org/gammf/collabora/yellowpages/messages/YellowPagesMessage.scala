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
  * Represents a registration request from an actor that wants to be included in the yellow pages.
  * @param actor the actor that sends the registration request.
  * @param topic the topic to which the actor asks to be registered.
  * @param service the service offered by the actor.
  */
case class RegistrationRequestMessage(actor: ActorRef, topic: Topic[TopicElement], service: ActorService)
  extends YellowPagesMessage

/**
  * Represents a confirmation of actor registration in the yellow pages.
  */
case class RegistrationOKMessage()
  extends YellowPagesMessage

/**
  * Represents an error in the actor registration in the yellow pages.
  */
case class RegistrationErrorMessage()
  extends YellowPagesMessage

/**
  * Represents an actor request from an actor, that asks for the reference of an actor with the specified topic and service.
  * @param topic the topic to which the requested actor have to be registered.
  * @param service the service offered by the requested actor.
  */
case class ActorRequestMessage(topic: Topic[TopicElement], service: ActorService)
  extends YellowPagesMessage

/**
  * Represents a positive response to an actor request message.
  * Contains a reference to an actor that meets all the requested specifications.
  * @param actor the reference to an actor with the specified topic and service.
  * @param topic the requested actor topic.
  * @param service the requested actor service.
  */
case class ActorOKMessage(actor: ActorRef, topic: Topic[TopicElement], service: ActorService)
  extends YellowPagesMessage

/**
  * Represents a negative response to an actor request message.
  * Should be used when an actor with the specified topic and service was not found in the yellow pages.
  * @param topic the topic requested in the actor request.
  * @param service the service requested in the actor request.
  */
case class ActorErrorMessage(topic: Topic[TopicElement], service: ActorService)
  extends YellowPagesMessage