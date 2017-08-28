package org.gammf.collabora.communication.actors

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import com.newmotion.akka.rabbitmq.{ConnectionActor, ConnectionFactory}
import org.gammf.collabora.communication.messages.{PublishFirebaseNotification, PublishNotificationMessage}
import org.gammf.collabora.database.actors.{ConnectionManagerActor, DBWorkerGetCollaborationActor}
import org.gammf.collabora.database.messages.GetCollaboration
import org.gammf.collabora.util.{UpdateMessage, UpdateMessageTarget, UpdateMessageType}
import us.raudi.pushraven.Notification
import us.raudi.pushraven.Pushraven

class FirebaseActor(collaborationGetter: ActorRef) extends Actor{

  private val AUTHORIZATION = "AAAAJtSw2Gk:APA91bEXmB5sRFqSnuYIP3qofHQ0RfHrAzTllJ0vYWtHXKZsMdbuXmUKbr16BVZsMO0cMmm_BWE8oLzkFcyuMr_V6O6ilqvLu7TrOgirVES51Ux9PsKfJ17iOMvTF_WtwqEURqMGBbLf"
  private[this] var info: Option[UpdateMessage] = None
  private[this] val notification: Notification = new Notification

  override def receive:Receive = {
    case PublishNotificationMessage(collaborationID, message) =>
      Pushraven.setKey(AUTHORIZATION)
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

object UseFirebaseActor extends App{

  implicit val system: ActorSystem = ActorSystem()
  val factory = new ConnectionFactory()
  var connectionManagerActor: ActorRef = system.actorOf(Props[ConnectionManagerActor])
  val connection:ActorRef = system.actorOf(ConnectionActor.props(factory), "rabbitmq")
  val naming: ActorRef = system.actorOf(Props[RabbitMQNamingActor], "naming")
  val channelCreator: ActorRef = system.actorOf(Props[ChannelCreatorActor], "channelCreator")
  val publisher: ActorRef = system.actorOf(Props[PublisherActor], "publisher")
  val collaborationMember: ActorRef = system.actorOf(Props(
    new CollaborationMembersActor(connection, naming, channelCreator, publisher)), "collaboration-members")
  var getCollaborarionsActor:ActorRef = system.actorOf(Props.create(classOf[DBWorkerGetCollaborationActor], connectionManagerActor, collaborationMember))
  var firebaseActor: ActorRef = system.actorOf(Props.create(classOf[FirebaseActor], getCollaborarionsActor))

  firebaseActor ! PublishNotificationMessage("123456788698540008900400",UpdateMessage(UpdateMessageTarget.NOTE,UpdateMessageType.UPDATING,"maffone"))
}
