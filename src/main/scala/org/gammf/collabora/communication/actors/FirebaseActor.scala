package org.gammf.collabora.communication.actors

import akka.actor.{ActorRef, Props}
import org.gammf.collabora.communication.messages.PublishNotificationMessage
import org.gammf.collabora.database.messages.GetCollaborationMessage
import org.gammf.collabora.util.{Collaboration, Firebase, UpdateMessage, UpdateMessageTarget, UpdateMessageType}
import org.gammf.collabora.yellowpages.ActorService.ActorService
import org.gammf.collabora.yellowpages.actors.BasicActor
import akka.pattern.ask
import org.gammf.collabora.yellowpages.util.Topic.ActorTopic
import org.gammf.collabora.yellowpages.util.Topic
import org.gammf.collabora.yellowpages.TopicElement._
import org.gammf.collabora.yellowpages.ActorService._

import scala.concurrent.ExecutionContext.Implicits.global

/**
  * This is an actor that sends Firebase notification about operations on notes, modules, members
  * to all the clients that are registered in the specific collaborationID Topic
  */
class FirebaseActor(override val yellowPages: ActorRef,
                    override val name: String,
                    override val topic: ActorTopic,
                    override val service: ActorService = Master) extends BasicActor{

  private[this] final val AUTHORIZATION = "AAAAJtSw2Gk:APA91bEXmB5sRFqSnuYIP3qofHQ0RfHrAzTllJ0vYWtHXKZsMdbuXmUKbr16BVZsMO0cMmm_BWE8oLzkFcyuMr_V6O6ilqvLu7TrOgirVES51Ux9PsKfJ17iOMvTF_WtwqEURqMGBbLf"
  private[this] val firebase: Firebase = new Firebase

  override def receive:Receive = ({
    case publishMessage: PublishNotificationMessage => publishMessage.message.target match {
            case UpdateMessageTarget.NOTE |
                 UpdateMessageTarget.MODULE |
                 UpdateMessageTarget.MEMBER =>
              getActorOrElse(Topic() :+ Database, Master, publishMessage).
                foreach(dbMaster =>
                  (dbMaster ? GetCollaborationMessage(publishMessage.collaborationID)).mapTo[Option[List[Collaboration]]].map {
                    case Some(head :: _) => sendFirebaseNotification(head, publishMessage.message)
                    case _ => println("[" + name + "] Notification error! Collaboration not found.")
                  })
            case _=>
    }
  }: Receive) orElse super[BasicActor].receive

  private[this] def sendFirebaseNotification(collaboration: Collaboration, message: UpdateMessage) {
    firebase.setKey(AUTHORIZATION)
    firebase.setTitle(collaboration.name)
    firebase.setBody(setUserAndOperation(message) + setTextTarget(message))
    firebase.to(collaboration.id.get)
    firebase.send()
  }

  private def setUserAndOperation(message: UpdateMessage): String = {
    message.user + (message.messageType match {
      case UpdateMessageType.CREATION => " added"
      case UpdateMessageType.UPDATING => " updated"
      case UpdateMessageType.DELETION => " deleted"
    })
  }

  private def setTextTarget(message: UpdateMessage): String = {
    message.target match {
      case UpdateMessageTarget.NOTE => " a note"
      case UpdateMessageTarget.MODULE => " a module"
      case UpdateMessageTarget.MEMBER=> " a member"
    }
  }
}

object FirebaseActor {
  /**
    * Factory method that returns a Props to create an already-registered firebase actor.
    * @param yellowPages the reference to the yellow pages root actor.
    * @param topic the topic to which this actor is going to be registered.
    * @return the Props to use to create a firebase actor.
    */

  def firebaseProps(yellowPages: ActorRef, topic: ActorTopic, name: String = "FirebaseActor") : Props =
    Props(new FirebaseActor(yellowPages = yellowPages, name = name, topic = topic))
}
