package org.gammf.collabora.yellowpages.util

import akka.actor.ActorRef
import org.gammf.collabora.yellowpages.ActorService.ActorService
import org.gammf.collabora.yellowpages.TopicElement.TopicElement

/**
  * Represents a generic yellow pages entry.
  * Contains the entity reference, the topic to which the entity is registered and the service offered by the entity.
  * @tparam A the generic type used to reference the entity.
  * @tparam B the generic type used to describe the topic element.
  * @tparam C the generic type used to describe the service.
  */
sealed trait YellowPagesEntry[A, B, C] {
  /**
    * Returns the reference to the entity registered to the service.
    */
  def reference: A

  /**
    * Returns the topic to which the entity is registered.
    */
  def topic: Topic[B]

  /**
    * Returns the service offered by the entity.
    */
  def service: C
}

/**
  * Represents a yellow pages entry in the actor world.
  * Contains the actor reference, the topic to which the actor is registered and the service offered by the actor.
  */
sealed trait ActorYellowPagesEntry extends YellowPagesEntry[ActorRef, TopicElement, ActorService]

/**
  * Simple implementation of a yellow pages entry in the actor world.
  * @param reference the reference to the actor registered to the service.
  * @param topic the topic to which the actor is registered.
  * @param service the service offered by the actor.
  */
case class ActorYellowPagesEntryImpl(override val reference: ActorRef, override val topic: Topic[TopicElement],
                                     override val service: ActorService) extends ActorYellowPagesEntry

object ActorYellowPagesEntry {
  def apply(reference: ActorRef, topic: Topic[TopicElement], service: ActorService): ActorYellowPagesEntry =
    ActorYellowPagesEntryImpl(reference, topic, service)
}
