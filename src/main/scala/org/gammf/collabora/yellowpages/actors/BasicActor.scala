package org.gammf.collabora.yellowpages.actors

import akka.actor.{Actor, ActorRef}
import akka.util.Timeout
import akka.pattern.ask
import org.gammf.collabora.yellowpages.ActorService.ActorService
import org.gammf.collabora.yellowpages.messages._
import org.gammf.collabora.yellowpages.util.{ActorInformation, CachableSet, Topic}
import org.gammf.collabora.yellowpages.util.Topic.ActorTopic
import org.gammf.collabora.yellowpages.entriesImplicitConversions._


import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

/**
  * Represents a simple actor identified by a name, registered to a [[Topic]], offering an [[ActorService]].
  * In order to communicate with other actors, this actor needs a reference to a [[YellowPagesActor]].
  */
trait BasicActor extends Actor {
  implicit protected[this] val askTimeout: Timeout = Timeout(5 seconds)

  protected val cachableRefs: CachableSet[ActorInformation] = CachableSet[ActorInformation]()
  private[this] val forwardTimeout: Timeout = Timeout(50 millis)

  /**
    * Returns a reference to the yellow pages root actor.
    */
  def yellowPages: ActorRef

  /**
    * Returns the name of this actor.
    */
  def name: String

  /**
    * Returns the topic to which this actor is registered.
    */
  def topic: ActorTopic

  /**
    * Returns the service offered by this actor.
    */
  def service: ActorService

  override def preStart(): Unit = super.preStart; yellowPages !
    RegistrationRequestMessage(reference = self, name = name, topic = topic, service = service)

  override def receive: Receive = {
    case RegistrationResponseMessage() => println("[" + name + "] Registration OK.")
    case InsertionErrorMessage() => println("[" + name + "] Insertion Error.")
    case _ => println("["+ name + "] Huh?"); unhandled(_)
  }

  /**
    * Universal method used by a [[BasicActor]] in order to get the [[ActorRef]] of an [[Actor]] which
    * is subscribed to a certain [[ActorTopic]] and which offers a certain [[ActorService]]. The research is first performed
    * on the local [[CachableSet]]. If it fails, the [[ActorRef]] of interest is asynchronously
    * asked to the yellow pages system.
    *
    * Since that this request could fail with a [[ActorResponseErrorMessage]], this method is designed to forward-to-self
    * (after a certain period of time) the message that triggered it, preserving the original sender.
    * With this approach, the actor will repeatedly ask for the [[Actor]]'s reference of interest and will perform its
    * operation once it gets it.
    * @param topic the topic to which the [[Actor]] of interest is subscribed to.
    * @param service the service offered by the [[Actor]] of interest.
    * @param message the message to forward-to-self in case the [[ActorRef]] is not available among the
    *                references in the cachable set.
    * @return [[Some(ActorRef)]] if the [[ActorRef]] is found, [[None]] otherwise.
    */
  protected[this] def getActorOrElse(topic: ActorTopic, service: ActorService, message: Any): Option[ActorRef] = {
    cachableRefs get (info => info.topic == topic && info.service == service) match {
      case Some((actorInformation, true)) => Some(actorInformation.reference)
      case Some((actorInformation, false)) => askYellowPagesForActor(topic, service); Some(actorInformation.reference)
      case _ =>
        val s = sender
        context.system.scheduler.scheduleOnce(forwardTimeout.duration)(self tell (message, s))
        askYellowPagesForActor(topic, service)
        None
    }
  }

  /**
    * Method used to ask asynchronously the yellow pages system for an [[Actor]], which is subscribed to a
    * certain [[ActorTopic]] and which offers a certain [[ActorService]], and to store its [[ActorRef]] in
    * the local cachable set.
    * @param topic the topic to which the [[Actor]] must be subscribed to.
    * @param service the service which the [[Actor]] must offer.
    */
  private[this] def askYellowPagesForActor(topic: ActorTopic, service: ActorService): Unit = {
    println("[" + name + "] asking yellow pages..")
    (yellowPages ? ActorRequestMessage(topic, service)).mapTo[ActorResponseMessage].map {
      case response: ActorResponseOKMessage => response :: cachableRefs
      case _ =>
    }
  }
}
