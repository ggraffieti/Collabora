package org.gammf.collabora.yellowpages.messages

import akka.actor.ActorRef
import org.gammf.collabora.yellowpages.ActorService.ActorService
import org.gammf.collabora.yellowpages.TopicElement.TopicElement
import org.gammf.collabora.yellowpages.util.Topic

/**
  * Represents a generic message about a yellow pages issue.
  */
trait YellowPagesMessage

trait InsertionRequestMessage extends YellowPagesMessage {
  def actor: ActorRef
  def topic: Topic[TopicElement]
  def service: ActorService
}

/**
  * Represents a registration request from an actor that wants to be included in the yellow pages.
  * @param actor the actor that sends the registration request.
  * @param topic the topic to which the actor asks to be registered.
  * @param service the service offered by the actor.
  */
case class RegistrationRequestMessage(actor: ActorRef, topic: Topic[TopicElement], service: ActorService)
  extends InsertionRequestMessage

/**
  * Represents a message between objects of type [[org.gammf.collabora.yellowpages.actors.YellowPagesActor]] containing the info about an entry.
  * An actor should send this message to delegate an entry management to another actor.
  * @param actor the actor of the entry.
  * @param topic the topic of the entry.
  * @param service the service of the entry.
  */
case class RedirectionRequestMessage(actor: ActorRef, topic: Topic[TopicElement], service: ActorService)
  extends InsertionRequestMessage

/**
  * Represents a request about a deletion of a yellow pages entries.
  * @param actor the actor to be unregistered.
  * @param topic the actor topic.
  * @param service the actor service.
  */
case class UnregistrationRequestMessage(actor: ActorRef, topic: Topic[TopicElement], service: ActorService)
  extends YellowPagesMessage

/**
  * Represents a positive response to an [[RedirectionRequestMessage]].
 *
  * @param actor the actor of the entry.
  * @param topic the topic of the entry.
  * @param service the service of the entry.
  */
case class ActorRedirectionOKMessage(actor: ActorRef, topic: Topic[TopicElement], service: ActorService)
  extends YellowPagesMessage

/**
  * Represents a negative response to an [[RedirectionRequestMessage]].
 *
  * @param actor the actor of the entry.
  * @param topic the topic of the entry.
  * @param service the service of the entry.
  */
case class ActorRedirectionErrorMessage(actor: ActorRef, topic: Topic[TopicElement], service: ActorService)
  extends YellowPagesMessage

/**
  * Represents a confirmation of actor registration in the yellow pages.
  * It's a positive response to a [[RegistrationRequestMessage]].
  */
case class RegistrationOKMessage()
  extends YellowPagesMessage

/**
  * Represents an actor request from an actor, that asks for the reference of an actor with the specified topic and service.
  * @param topic the topic to which the requested actor have to be registered.
  * @param service the service offered by the requested actor.
  */
case class ActorRequestMessage(topic: Topic[TopicElement], service: ActorService)
  extends YellowPagesMessage

/**
  * Represents a positive response to an [[ActorRequestMessage]].
  * Contains a reference to an actor that meets all the requested specifications.
  * @param actor the reference to an actor with the specified topic and service.
  * @param topic the requested actor topic.
  * @param service the requested actor service.
  */
case class ActorOKMessage(actor: ActorRef, topic: Topic[TopicElement], service: ActorService)
  extends YellowPagesMessage

/**
  * Represents an error in the actor registration in the yellow pages.
  * It's a negative response to a [[RegistrationRequestMessage]].
  */
case class RegistrationErrorMessage() extends YellowPagesMessage

/**
  * Represents a negative response to an [[ActorRequestMessage]].
  * Should be used when an actor with the specified topic and service was not found in the yellow pages.
  * @param topic the topic requested in the actor request.
  * @param service the service requested in the actor request.
  */
case class ActorErrorMessage(topic: Topic[TopicElement], service: ActorService) extends YellowPagesMessage