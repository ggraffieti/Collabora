package org.gammf.collabora.yellowpages.actors

import akka.actor.{Actor, ActorRef, Props}
import akka.util.Timeout
import akka.pattern.ask

import scala.concurrent.Await
import org.gammf.collabora.yellowpages.messages._
import org.gammf.collabora.yellowpages.util.ActorYellowPagesEntry
import org.gammf.collabora.yellowpages.ActorService._
import org.gammf.collabora.yellowpages.util.Topic.ActorTopic

import scala.annotation.tailrec
import scala.concurrent.duration._
import scala.language.postfixOps

/**
  * This is an actor that deals with the yellow pages service.
  * Can handle registration requests and actor requests, with the help of some other yellow pages actors.
  * A yellow pages actor can manage even the registration of other yellow pages actors. In this way more yellow pages actors
  * can cooperate, splitting up the work, in order to form a structured hierarchy of yellow pages, each one containing
  * a subset of the actors of the global actor system.
  */
sealed trait YellowPagesActor extends Actor {
  def name: String
  private[this] var yellowPages: List[ActorYellowPagesEntry] = List()
  implicit val timeout: Timeout = Timeout(5 seconds)

  import org.gammf.collabora.yellowpages.entriesImplicitConversions._
  override def receive: Receive = {
    case msg: InsertionRequestMessage => handleActorInsertion(msg)
    case msg: DeletionRequestMessage => handleActorDeletion(msg)
    case msg: ActorRequestMessage => handleActorRequest(msg)
    case msg: HierarchyRequestMessage => handleHierarchy(msg.level)
  }

  private[this] def handleActorInsertion(msg: InsertionRequestMessage): Unit = {
    searchForValidYPActor(msg)
    def searchForValidYPActor(msg: InsertionRequestMessage): Unit = yellowPages.filter(yp => yp > msg && yp.service == YellowPagesService) match {
        case h :: _ => h.reference forward msg
        case _ => evaluateActorInsertion(msg)
      }
    def evaluateActorInsertion(msg: InsertionRequestMessage): Unit = msg.service match {
        case YellowPagesService => if (yellowPages.exists(_ === msg)) sender ! InsertionErrorMessage() else insertYPActor(msg)
        case _ => insertSimpleActor(msg)
    }
    def insertYPActor(msg: InsertionRequestMessage): Unit = { insertSimpleActor(msg); delegateActorsToNewYPActor(msg) }
    def insertSimpleActor(msg: InsertionRequestMessage): Unit = { yellowPages = msg :: yellowPages; sender ! buildResponse(msg) }
    def buildResponse(msg: InsertionRequestMessage): InsertionResponseMessage = msg match {
      case _: RegistrationRequestMessage => RegistrationResponseMessage()
      case RedirectionRequestMessage(r, n, t, s) => RedirectionResponseMessage(r, n, t, s)
    }
    def delegateActorsToNewYPActor(msg: InsertionRequestMessage): Unit = yellowPages.filter(_ < msg).foreach(yp => {
      Await.result(msg.reference ? (yp: RedirectionRequestMessage), timeout.duration).asInstanceOf[InsertionResponseMessage] match {
        case m: RedirectionResponseMessage => yellowPages = yellowPages.filterNot(_ == (m: ActorYellowPagesEntry))
        case _ => // If you don't want the actor reference it's fine, I'll keep it
      }
    })
  }

  private[this] def handleActorRequest(msg: ActorRequestMessage): Unit = {
    searchForActor(yellowPages.filter(_ === msg), msg)
    def searchForActor(list: List[ActorYellowPagesEntry], msg: ActorRequestMessage): Unit = {
      if (list.forall(_.used)) list.foreach(_.used = false)
      searchForLeastRecentlyUsedActor(list, msg)
    }
    @tailrec def searchForLeastRecentlyUsedActor(list: List[ActorYellowPagesEntry], msg: ActorRequestMessage): Unit = list match {
        case h :: t if h.used => searchForLeastRecentlyUsedActor(t, msg)
        case h :: _ if !h.used => sender ! (h: ActorResponseOKMessage); h.used = true
        case _ => searchForValidYPActor(msg)
    }
    def searchForValidYPActor(msg: ActorRequestMessage): Unit = yellowPages.filter(yp => yp > msg && yp.service == YellowPagesService) match {
      case h :: _ => h.reference forward msg
      case _ => sender ! ActorResponseErrorMessage()
    }
  }

  private[this] def handleActorDeletion(msg: ActorYellowPagesEntry): Unit = yellowPages.filter(yp => yp == msg) match {
    case h :: _ => yellowPages = yellowPages.filterNot(yp => yp == h)
    case _ => yellowPages.filter(yp => yp > msg && yp.service == YellowPagesService).foreach(yp => yp.reference forward (msg : DeletionRequestMessage))
  }

  private[this] def handleHierarchy(lvl: Int): Unit = {
    this match {
      case _: RootYellowPagesActor => printHierarchy(getActors(lvl + 1))
      case _ => sender ! HierarchyResponseMessage(getActors(lvl + 1))
    }
    def getActors(level: Int): List[(Int, ActorYellowPagesEntry)] = {
      def searchActors(actors: List[(Int, ActorYellowPagesEntry)]): List[(Int, ActorYellowPagesEntry)] = actors match {
        case h :: t if h._2.service != YellowPagesService => h :: searchActors(t)
        case h :: t if h._2.service == YellowPagesService => h :: getActorsFromYP(h._2.reference) ++ searchActors(t)
        case _ => Nil
      }
      def getActorsFromYP(yp: ActorRef): List[(Int, ActorYellowPagesEntry)] = Await.result(yp ? HierarchyRequestMessage(level), timeout.duration).asInstanceOf[HierarchyResponseMessage].actors
      searchActors(yellowPages.map((level, _)))
    }
    def printHierarchy(list: List[(Int, ActorYellowPagesEntry)]): Unit = {
      def getRoot: HierarchyNode = HierarchyNode(level = lvl, reference = self.toString(), name = name, topic = "/", service = "YellowPagesService")
      yellowPages.find(_.service == Printing).foreach(_.reference ! HierarchyPrintMessage(getRoot :: (list: List[HierarchyNode])))
    }
  }
}

/**
  * Represents the master of the [[YellowPagesActor]].
  * Every yellow pages related request from any actor should be sent to this actor, in particular registration requests and actor requests.
  * A complex actor system should be based on a structured yellow pages service, built on mutiple levels. Despite this,
  * every actor of the system should only interact with this actor, that will forward all the requests to the yellow pages
  * of the appropriate level.
  * @param name the name of the yellow pages root actor.
  */
case class RootYellowPagesActor(override val name: String) extends YellowPagesActor

/**
  * A generic actor that offer a yellow pages service.
  * As every other actor, this actor have to relate to the [[RootYellowPagesActor]]. Becomes useful to create a yellow pages
  * hierarchy, managing all the requests on a certain [[org.gammf.collabora.yellowpages.util.Topic.ActorTopic]].
  * @param yellowPages the reference to the yellow pages root actor.
  * @param name the name of this actor.
  * @param topic the topic to which this actor is registered.
  * @param service the service that this actor offers.
  */
case class TopicYellowPagesActor(override val yellowPages: ActorRef,
                                 override val name: String,
                                 override val topic: ActorTopic,
                                 override val service: ActorService = YellowPagesService)
  extends BasicActor with YellowPagesActor {
  override def receive: Receive = super[YellowPagesActor].receive orElse super[BasicActor].receive
}

object YellowPagesActor {
  /**
    * Factory methods that returns a Props to create a yellow pages root actor.
    * @return the Props to use to create a yellow pages root actor.
    */
  def rootProps(): Props = Props(RootYellowPagesActor(name = "Root_YellowPages"))

  /**
    * Factory methods that returns a Props to create a yellow pages actor registered to the specified topic.
    * @param yellowPages the reference to the yellow pages root actor.
    * @param topic the topic to which this actor is going to be registered.
    * @return the Props to use to create a yellow pages topic actor.
    */
  def topicProps(yellowPages: ActorRef, topic: ActorTopic, name: String = "Topic_YellowPages"): Props =
    Props(TopicYellowPagesActor(yellowPages = yellowPages, name = name, topic = topic))
}
