package org.gammf.collabora.communication.actors

import akka.actor.{Actor, ActorRef}
import org.gammf.collabora.communication.messages.{PublishFirebaseNotification, PublishNotificationMessage}
import org.gammf.collabora.database.messages.GetCollaboration
import org.gammf.collabora.util.{ UpdateMessage, UpdateMessageTarget, UpdateMessageType}
import us.raudi.pushraven.Notification
import us.raudi.pushraven.Pushraven

class FirebaseActor(collaborationGetter: ActorRef) extends Actor{

  private[this] var info: Option[UpdateMessage] = None
  private[this] val notification: Notification = Notification

  override def receive:Receive = {
    case PublishNotificationMessage(collaborationID, message) =>
      info = Some(message)
      collaborationGetter ! GetCollaboration(collaborationID)
    case PublishFirebaseNotification(collaborationID,collaboration)=>
      notification.title(collaboration.name)
                  .text(setTextType()+setTextTarget())
                  .to(collaborationID)
      val response = Pushraven.push(notification)
      System.out.println(response)
      notification.clear()
    case _ => println("[FirebaseActor] Huh?")
  }

  private def setTextType(): String = {
    val notifyText: String = info.get.user
      info.get.messageType match {
        case UpdateMessageType.CREATION => notifyText + " added"
        case UpdateMessageType.UPDATING => notifyText + " updated"
        case UpdateMessageType.DELETION => notifyText + " deleted"
      }
  }

  private def setTextTarget(): String = {
    info.get.target match {
      case UpdateMessageTarget.NOTE => " a note"
      case UpdateMessageTarget.MODULE => " a module"
      case UpdateMessageTarget.MEMBER=> " a member"
    }
  }

}
