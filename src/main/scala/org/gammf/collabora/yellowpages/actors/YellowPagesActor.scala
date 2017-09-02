package org.gammf.collabora.yellowpages.actors

import akka.actor.Actor
import org.gammf.collabora.yellowpages.messages._
import org.gammf.collabora.yellowpages.util.{ActorYellowPagesEntry, Topic}
import org.gammf.collabora.yellowpages.ActorService._
import org.gammf.collabora.yellowpages.TopicElement.TopicElement

/**
  * @author Manuel Peruzzi
  * This is an actor that deals with the yellow pages service.
  * Can handle registration requests and actor requests, with the help of some other yellow pages actors.
  */
class YellowPagesActor extends Actor {
  private[this] var yellowPages: List[ActorYellowPagesEntry] = List()
  private[this] val getValidYellowPagesActors: Topic[TopicElement] => List[ActorYellowPagesEntry] =
    topic => yellowPages.filter(yp => topic > yp.topic && yp.service == yellowPagesService)

  override def receive: Receive = {
    case msg: RegistrationRequestMessage => getValidYellowPagesActors(msg.topic) match {
      case h :: _ => h.reference forward msg
      case _ => yellowPages = msg :: yellowPages; msg.reference ! RegistrationOKMessage
    }

    case msg: ActorRequestMessage => yellowPages.filter(yp => msg.topic == yp.topic && msg.service == yp.service) match {
      case h :: _ => sender ! h.asInstanceOf[ActorOKMessage]
      case _ => getValidYellowPagesActors(msg.topic) match {
        case h :: _ => h.reference forward msg
        case _ => sender ! ActorErrorMessage
      }
    }

    case _ => println("[YellowPagesActor] Huh?")
  }
}
