package org.gammf.collabora.yellowpages.messages

import akka.actor.ActorRef
import org.gammf.collabora.yellowpages.ActorService.ActorService
import org.gammf.collabora.yellowpages.util.Topic.ActorTopic

/**
  * Represents an actor request from an actor, that asks for the reference of an actor with the specified
  * [[org.gammf.collabora.yellowpages.util.Topic.ActorTopic]] and [[ActorService]].
  *
  * @param topic the topic to which the requested actor have to be registered.
  * @param service the service offered by the requested actor.
  */
case class ActorRequestMessage(topic: ActorTopic, service: ActorService) extends YellowPagesMessage

/**
  * Represents a response to an [[ActorRequestMessage]].
  */
sealed trait ActorResponseMessage

/**
  * Represents a positive response to an [[ActorRequestMessage]].
  * Contains a reference to an actor that meets all the requested specifications.
  * @param actor the reference to an actor with the specified topic and service.
  * @param topic the requested actor topic.
  * @param service the requested actor service.
  */
case class ActorResponseOKMessage(actor: ActorRef, topic: ActorTopic, service: ActorService) extends ActorResponseMessage

/**
  * Represents a negative response to an [[ActorRequestMessage]].
  * Should be used when an actor with the specified topic and service was not found in the yellow pages.
  */
case class ActorResponseErrorMessage() extends ActorResponseMessage
