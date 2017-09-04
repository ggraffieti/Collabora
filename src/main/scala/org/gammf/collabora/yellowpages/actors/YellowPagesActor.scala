package org.gammf.collabora.yellowpages.actors

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import org.gammf.collabora.yellowpages.messages._
import org.gammf.collabora.yellowpages.util.{ActorYellowPagesEntry, Topic}
import org.gammf.collabora.yellowpages.ActorService._
import org.gammf.collabora.yellowpages.TopicElement._

/**
  * @author Manuel Peruzzi
  * This is an actor that deals with the yellow pages service.
  * Can handle registration requests and actor requests, with the help of some other yellow pages actors.
  */
trait YellowPagesActor extends Actor {
  val name: String
  private[this] var yellowPages: List[ActorYellowPagesEntry] = List()
  private[this] val getValidYellowPagesActors: Topic[TopicElement] => List[ActorYellowPagesEntry] =
    topic => yellowPages.filter(yp => yp.topic > topic && yp.service == YellowPagesService)

  override def receive: Receive = {
    case msg: RegistrationRequestMessage => getValidYellowPagesActors(msg.topic) match {
      case h :: _ => h.reference forward msg
      case _ => yellowPages = msg :: yellowPages; sender ! RegistrationOKMessage(); println("[" + name + "]" + yellowPages)
    }

    case msg: ActorRequestMessage => yellowPages.filter(yp => msg.topic == yp.topic && msg.service == yp.service && !yp.used) match {
      case h :: t => sender ! (h: ActorOKMessage); h.used = true; if ((h :: t).forall(yp => yp.used)) (h :: t).foreach(yp => yp.used = false)
      case _ => getValidYellowPagesActors(msg.topic) match {
        case h :: _ => h.reference forward msg
        case _ => sender ! (msg: ActorErrorMessage)
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
  extends BasicActor with YellowPagesActor

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
