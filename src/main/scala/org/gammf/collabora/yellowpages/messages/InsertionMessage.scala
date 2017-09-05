package org.gammf.collabora.yellowpages.messages

import akka.actor.ActorRef
import org.gammf.collabora.yellowpages.ActorService.ActorService
import org.gammf.collabora.yellowpages.TopicElement.TopicElement
import org.gammf.collabora.yellowpages.util.Topic

/**
  * Represents a request from an actor to a [[org.gammf.collabora.yellowpages.actors.YellowPagesActor]] that leads to an entry insertion in the yellow pages.
  */
sealed trait InsertionRequestMessage extends YellowPagesMessage {

  /**
    * Returns the actor that needs to be inserted.
    */
  def actor: ActorRef

  /**
    * Returns the topic to which the actor is registered.
    */
  def topic: Topic[TopicElement]

  /**
    * Returns the service offered by the actor.
    */
  def service: ActorService
}

/**
  * Represents a registration request from an actor that wants to be included in the yellow pages.
  * @param actor the actor that sends the registration request.
  * @param topic the topic to which the actor asks to be registered.
  * @param service the service offered by the actor.
  */
case class RegistrationRequestMessage(override val actor: ActorRef, override val topic: Topic[TopicElement],
                                      override val service: ActorService) extends InsertionRequestMessage

/**
  * Represents a message between objects of type [[org.gammf.collabora.yellowpages.actors.YellowPagesActor]] containing the info about an entry.
  * An actor should send this message to delegate an entry management to another actor.
  * @param actor the actor of the entry.
  * @param topic the topic of the entry.
  * @param service the service of the entry.
  */
case class RedirectionRequestMessage(override val actor: ActorRef, override val topic: Topic[TopicElement],
                                     override val service: ActorService) extends InsertionRequestMessage

/**
  * It's a response to an [[InsertionRequestMessage]].
  */
sealed trait InsertionResponseMessage extends YellowPagesMessage

/**
  * Represents a confirmation of actor registration in the yellow pages.
  * It's a response to a [[RegistrationRequestMessage]].
  */
case class RegistrationResponseMessage() extends InsertionResponseMessage

/**
  * Reprsents a confirmation of actor redirection in the yellow pages.
  * It's a response to an [[RedirectionRequestMessage]].
  *
  * @param actor the actor of the entry.
  * @param topic the topic of the entry.
  * @param service the service of the entry.
  */
case class RedirectionResponseMessage(actor: ActorRef, topic: Topic[TopicElement], service: ActorService) extends InsertionResponseMessage
