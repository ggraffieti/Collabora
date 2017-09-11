package org.gammf.collabora.yellowpages.actors

import akka.actor.{Actor, ActorRef}
import akka.util.Timeout
import akka.pattern.ask
import org.gammf.collabora.yellowpages.ActorService.ActorService
import org.gammf.collabora.yellowpages.messages._
import org.gammf.collabora.yellowpages.util.{ActorInformation, CachableSet, Topic}
import org.gammf.collabora.yellowpages.util.Topic.ActorTopic

import scala.concurrent.duration._
import scala.util.{Success, Try}
import scala.concurrent.ExecutionContext.Implicits.global

/**
  * Represents a simple actor identified by a name, registered to a [[Topic]], offering an [[ActorService]].
  * In order to communicate with other actors, this actor needs a reference to a [[YellowPagesActor]].
  */
trait BasicActor extends Actor {
  implicit private[this] val askTimeout: Timeout = Timeout(5 seconds)

  protected val cachableRefs: CachableSet[ActorInformation] = CachableSet[ActorInformation]()
  private[this] val forwardTimeout: Timeout = Timeout(500 millis)

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

  private[this] def askYellowPagesForActor(topic: ActorTopic, service: ActorService): Unit = {
    (yellowPages ? ActorRequestMessage(topic, service)).mapTo[ActorResponseMessage].map {
      case response: ActorResponseOKMessage => response :: cachableRefs
      case _ =>
    }
  }

  protected[this] def sendMessageOrElse(reference: Try[ActorRef], message: Any, otherMessage: Any): Unit = reference match {
    case Success(actorRef) => actorRef ! message
    case _ => context.system.scheduler.scheduleOnce(forwardTimeout.duration)(self forward otherMessage)
  }
}
