package org.gammf.collabora.yellowpages.messages

import akka.actor.ActorRef
import org.gammf.collabora.yellowpages.ActorService.ActorService
import org.gammf.collabora.yellowpages.actors.YellowPagesActor
import org.gammf.collabora.yellowpages.util.Topic.ActorTopic

/**
  * Represents a request from an actor to a [[YellowPagesActor]] that leads to an entry insertion in the yellow pages.
  */
sealed trait InsertionRequestMessage extends YellowPagesMessage {

  /**
    * Returns the reference to the actor that needs to be inserted.
    */
  def reference: ActorRef

  /**
    * Returns the name of the actor that needs to be inserted.
    */
  def name: String

  /**
    * Returns the topic to which the actor is registered.
    */
  def topic: ActorTopic

  /**
    * Returns the service offered by the actor.
    */
  def service: ActorService
}

/**
  * Represents a registration request from an actor that wants to be included in the yellow pages.
  * @param reference the reference to the actor that sends the registration request.
  * @param name the name of the actor that sends the registration request.
  * @param topic the topic to which the actor asks to be registered.
  * @param service the service offered by the actor.
  */
case class RegistrationRequestMessage(override val reference: ActorRef, override val name: String,
                                      override val topic: ActorTopic, override val service: ActorService)
  extends InsertionRequestMessage

/**
  * Represents a message between objects of type [[org.gammf.collabora.yellowpages.actors.YellowPagesActor]] containing the info about an entry.
  * An actor should send this message to delegate an entry management to another actor.
  * @param reference the reference of the actor of the entry.
  * @param name the name of the actor of the entry.
  * @param topic the topic of the entry.
  * @param service the service of the entry.
  */
case class RedirectionRequestMessage(override val reference: ActorRef, override val name: String,
                                     override val topic: ActorTopic, override val service: ActorService)
  extends InsertionRequestMessage

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
  * Represents a confirmation of actor redirection in the yellow pages.
  * It's a response to an [[RedirectionRequestMessage]].
  *
  * @param reference the reference to the actor of the entry.
  * @param name the name of the actor of the entry.
  * @param topic the topic of the entry.
  * @param service the service of the entry.
  */
case class RedirectionResponseMessage(reference: ActorRef, name: String, topic: ActorTopic, service: ActorService)
  extends InsertionResponseMessage
