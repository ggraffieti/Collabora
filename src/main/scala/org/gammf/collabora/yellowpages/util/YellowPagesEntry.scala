package org.gammf.collabora.yellowpages.util

import akka.actor.ActorRef
import org.gammf.collabora.yellowpages.ActorService._
import org.gammf.collabora.yellowpages.TopicElement._

import language.reflectiveCalls

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

  /**
    * Returns a boolean flag stating if the entry has been used in the recent time.
    * @return true if the entry has been used recently, false otherwise.
    */
  def used: Boolean

  /**
    * Sets a boolean flag stating if the entry has been used in the recent time.
    * @param used a boolean flag stating if the entry has been used recently.
    */
  def used_= (used: Boolean): Unit
}

/**
  * Represents a yellow pages entry in the actor world.
  * Contains the actor reference, the topic to which the actor is registered and the service offered by the actor.
  */
sealed trait ActorYellowPagesEntry extends YellowPagesEntry[ActorRef, TopicElement, ActorService] {
  /**
    * Similarity method. Compares this entry to some other entry.
    * @param that the other entry to be compared to this entry.
    * @return true if the given entry is similar to this entry, false otherwise.
    */
  def ===(that: { def topic: Topic[TopicElement]; def service: ActorService }): Boolean

  /**
    * Greater method. Checks if this entry is greater than another object, based on its topic value.
    * @param that the object to be compared to this entry.
    * @return true if this entry is greater than the given object, false otherwise.
    */
  def >(that: { def topic: Topic[TopicElement]; def service: ActorService }): Boolean
  /**
    * Greater or similar method. Checks if this entry is similar or greater than another object, based on its topic value.
    * @param that the object to be compared to this entry.
    * @return true if this entry is similar or greater than the given object, false otherwise.
    */
  def >=(that: { def topic: Topic[TopicElement]; def service: ActorService }): Boolean

  /**
    * Lesser method. Checks if this entry is lesser than another object, based on its topic value.
    * @param that the object to be compared to this entry.
    * @return true if this entry is lesser than the given object, false otherwise.
    */
  def <(that: { def topic: Topic[TopicElement]; def service: ActorService }): Boolean

  /**
    * Lesser or similar method. Checks if this entry is similar or lesser than another object, based on its topic value.
    * @param that the object to be compared to this entry.
    * @return true if this entry is similar or lesser than the given object, false otherwise.
    */
  def <=(that: { def topic: Topic[TopicElement]; def service: ActorService }): Boolean
}

/**
  * Simple implementation of a yellow pages entry in the actor world.
  * @param reference the reference to the actor registered to the service.
  * @param topic the topic to which the actor is registered.
  * @param service the service offered by the actor.
  * @param used a boolean flag stating if the entry has been used recently.
  */
case class ActorYellowPagesEntryImpl(override val reference: ActorRef, override val topic: Topic[TopicElement],
                                     override val service: ActorService, override var used: Boolean = false)
  extends ActorYellowPagesEntry {
  override def equals(obj: Any): Boolean = obj match {
    case e: ActorYellowPagesEntry => e.reference == reference && e.topic == topic && e.service == service
  }
  override def ===(that: { def service: ActorService; def topic: Topic[TopicElement]}): Boolean =
    topic == that.topic && service == that.service
  override def >(that: { def service: ActorService; def topic: Topic[TopicElement]}): Boolean =
    (that.service == YellowPagesService && topic > that.topic) || (that.service != YellowPagesService && topic >= that.topic)
  override def >=(that: { def service: ActorService; def topic: Topic[TopicElement]}): Boolean =
    this > that || this === that
  override def <(that: { def service: ActorService; def topic: Topic[TopicElement]}): Boolean =
    (that.service == YellowPagesService && topic < that.topic) || (that.service != YellowPagesService && topic <= that.topic)
  override def <=(that: { def service: ActorService; def topic: Topic[TopicElement]}): Boolean =
    this < that || this === that
}

object ActorYellowPagesEntry {
  /**
    * Apply method to build an [[ActorYellowPagesEntry]] object.
    * @param reference the actor reference.
    * @param topic the topic to which the actor is registered.
    * @param service the service offered by the actor.
    * @return a new instance of [[ActorYellowPagesEntry]].
    */
  def apply(reference: ActorRef, topic: Topic[TopicElement], service: ActorService): ActorYellowPagesEntry =
    ActorYellowPagesEntryImpl(reference, topic, service)
}
