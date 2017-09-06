package org.gammf.collabora.yellowpages.actors

import akka.actor.{Actor, ActorRef, Props}
import akka.util.Timeout
import org.gammf.collabora.yellowpages.messages._
import org.gammf.collabora.yellowpages.util.{ActorYellowPagesEntry, Topic}
import org.gammf.collabora.yellowpages.ActorService._
import org.gammf.collabora.yellowpages.TopicElement._

import scala.concurrent.Await
import scala.concurrent.duration.Duration
import akka.pattern.ask

/**
  * @author Manuel Peruzzi
  * This is an actor that deals with the yellow pages service.
  * Can handle registration requests and actor requests, with the help of some other yellow pages actors.
  */
trait YellowPagesActor extends Actor {
  val name: String
  private[this] var yellowPages: List[ActorYellowPagesEntry] = List()

  import org.gammf.collabora.yellowpages.entriesImplicitConversions._
  override def receive: Receive = {
    case msg: RegistrationRequestMessage => handleActorInsertion(msg)
    case msg: RedirectionRequestMessage => handleActorInsertion(msg)
    case msg: RedirectionResponseMessage => handleActorDeletion(msg)
    case msg: DeletionRequestMessage => handleActorDeletion(msg)
    case msg: ActorRequestMessage => handleActorRequest(msg)
    case msg: HierarchyRequestMessage => handleHierarchy(msg.level)
  }

  private[this] def handleActorInsertion(msg: InsertionRequestMessage): Unit = {
    searchForValidYPActor(msg)
    def searchForValidYPActor(msg: InsertionRequestMessage): Unit = yellowPages.filter(yp => yp > msg && yp.service == YellowPagesService) match {
        case h :: _ => h.reference forward msg //TODO load balancing about yellow pages actors
        case _ => insertActor(msg)
      }
    def insertActor(msg: InsertionRequestMessage): Unit = {
      yellowPages = msg :: yellowPages; sender ! buildInsertionResponse(msg)
      if (msg.service == YellowPagesService) delegateActorsToNewYPActor(msg)
    }
    def buildInsertionResponse(msg: InsertionRequestMessage): InsertionResponseMessage = msg match {
      case _: RegistrationRequestMessage => RegistrationResponseMessage()
      case RedirectionRequestMessage(r, n, t, s) => RedirectionResponseMessage(r, n, t, s)
    }
    def delegateActorsToNewYPActor(msg: InsertionRequestMessage): Unit =
      yellowPages.filter(yp => yp < msg).foreach(yp => msg.reference ! (yp: RedirectionRequestMessage))
  }

  private[this] def handleActorRequest(msg: ActorRequestMessage): Unit = {
    searchForActor(yellowPages.filter(yp => yp === msg), msg)
    def searchForActor(list: List[ActorYellowPagesEntry], msg: ActorRequestMessage): Unit = {
      if (list.forall(yp => yp.used)) list.foreach(yp => yp.used = false)
      list match {
        case h :: t if h.used => searchForActor(t, msg)
        case h :: _ if !h.used => sender ! (h: ActorResponseOKMessage); h.used = true
        case _ => searchForValidYPActor(msg)
      }
    }
    def searchForValidYPActor(msg: ActorRequestMessage): Unit = yellowPages.filter(yp => yp > msg && yp.service == YellowPagesService) match {
      case h :: _ => h.reference forward msg //TODO load balancing about yellow pages actors
      case _ => sender ! ActorResponseErrorMessage()
    }
  }

  private[this] def handleActorDeletion(msg: ActorYellowPagesEntry): Unit = yellowPages = yellowPages.filterNot(yp => yp == msg)

  private[this] def handleHierarchy(l: Int): Unit = {
    val level = l+1; var list: List[(Int, ActorYellowPagesEntry)] = List()
    list = list ++ yellowPages.map(yp => (level, yp))
    yellowPages.filter(yp => yp.service == YellowPagesService).foreach(yp => list = list ++ askYellowPagesActors(yp.reference))
    def askYellowPagesActors(yp: ActorRef): List[(Int, ActorYellowPagesEntry)] = {
      implicit val timeout: Timeout = Timeout(Duration(5, "seconds"))
      Await.result(yp ? HierarchyRequestMessage(level), timeout.duration).asInstanceOf[HierarchyResponseMessage].actors
    }
    this match {
      case _: RootYellowPagesActor => printHierarchy()
      case _ => sender ! HierarchyResponseMessage(list)
    }
    def printHierarchy(): Unit = {
      yellowPages.filter(yp => yp.service == Printing) match {
        case h :: _ => val myself: HierarchyNode = HierarchyNode(l, self.toString(), "General", "RootYellowPagesService")
          h.reference ! HierarchyPrintMessage(myself :: (list: List[HierarchyNode]))
        case _ => println(list)
      }
    }
  }
}

/**
  * The root of the yellow pages services.
  * Every yellow pages related request should be sent to this actor.
  * @param name the name of the yellow pages root actor.
  */
case class RootYellowPagesActor(override val name: String) extends YellowPagesActor

/**
  * A generic actor that offer a yellow pages service.
  * @param yellowPages the reference to the yellow pages root actor.
  * @param name the name of this actor.
  * @param topic the topic to which this actor is registered.
  * @param service the service that this actor offers.
  */
case class TopicYellowPagesActor(override val yellowPages: ActorRef,
                                 override val name: String,
                                 override val topic: Topic[TopicElement],
                                 override val service: ActorService = YellowPagesService)
  extends BasicActor with YellowPagesActor {
  override def receive: Receive = super[YellowPagesActor].receive orElse super[BasicActor].receive
}

object YellowPagesActor {
  /**
    * Factory methods that returns a Props to create a yellow pages root actor.
    * @return the Props to use to create a yellow pages root actor.
    */
  def rootProps(): Props = Props(RootYellowPagesActor(name = "RootYellowPagesActor"))

  /**
    * Factory methods that returns a [[Props]] to create a yellow pages actor registered to the specified topic.
    * @param yellowPages the reference to the yellow pages root actor.
    * @param topic the topic to which this actor is going to be registered.
    * @return the [[Props]] to use to create a yellow pages topic actor.
    */
  def topicProps(yellowPages: ActorRef, topic: Topic[TopicElement]): Props = Props(TopicYellowPagesActor(
    yellowPages = yellowPages, name = topic + "YellowPagesActor", topic = topic))
}
