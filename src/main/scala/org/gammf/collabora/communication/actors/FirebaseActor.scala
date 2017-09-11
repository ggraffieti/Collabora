package org.gammf.collabora.communication.actors

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import com.newmotion.akka.rabbitmq.{ConnectionActor, ConnectionFactory}
import org.gammf.collabora.communication.messages.{PublishFirebaseNotification, PublishNotificationMessage}
import org.gammf.collabora.database.actors.ConnectionManagerActor
import org.gammf.collabora.database.actors.worker.DBWorkerGetCollaborationActor
import org.gammf.collabora.database.messages.GetCollaboration
import org.gammf.collabora.util.{Firebase, UpdateMessage, UpdateMessageTarget, UpdateMessageType}
import org.gammf.collabora.yellowpages.ActorService.ActorService
import org.gammf.collabora.yellowpages.actors.BasicActor
import org.gammf.collabora.yellowpages.util.Topic.ActorTopic

/**
  * This is an actor that sends Firebase notification about Note,Module,Member operation
  * to all the client that are registered in the specific collaborationID Topic
  */
class FirebaseActor(override val yellowPages: ActorRef, override val name: String,
                    override val topic: ActorTopic, override val service: ActorService) extends BasicActor{

  private[this] final val AUTHORIZATION = "AAAAJtSw2Gk:APA91bEXmB5sRFqSnuYIP3qofHQ0RfHrAzTllJ0vYWtHXKZsMdbuXmUKbr16BVZsMO0cMmm_BWE8oLzkFcyuMr_V6O6ilqvLu7TrOgirVES51Ux9PsKfJ17iOMvTF_WtwqEURqMGBbLf"
  private[this] val firebase: Firebase = new Firebase

  override def receive:Receive = ({
    case PublishNotificationMessage(collaborationID, message) => message.target match {
            case UpdateMessageTarget.NOTE |
                 UpdateMessageTarget.MODULE |
                 UpdateMessageTarget.MEMBER if message.messageType.equals(UpdateMessageType.CREATION) => sendFirebaseNotification(collaborationID, message)
            case _=>
    }
  }: Receive) orElse super[BasicActor].receive

  private[this] def sendFirebaseNotification(collaborationID: String, message: UpdateMessage) {
    firebase.setKey(AUTHORIZATION)
    firebase.setTtile(collaborationID)
    firebase.setBody(setUserAndOperation(message) + setTextTarget(message))
    firebase.to(collaborationID)
    firebase.send()
  }

  /**
    * Method used to insert in the message the User and the operation that the user requested
    * @return
    */
  private def setUserAndOperation(message: UpdateMessage): String = {
    message.user + (message.messageType match {
      case UpdateMessageType.CREATION => " added"
      case UpdateMessageType.UPDATING => " updated"
      case UpdateMessageType.DELETION => " deleted"
    })
  }

  /**
    * Method used to insert in the message the object of the requested operation
    * @return
    */
  private def setTextTarget(message: UpdateMessage): String = {
    message.target match {
      case UpdateMessageTarget.NOTE => " a note"
      case UpdateMessageTarget.MODULE => " a module"
      case UpdateMessageTarget.MEMBER=> " a member"
    }
  }

}

object UseFirebaseActor extends App{
  //TODO refactoring
  implicit val system: ActorSystem = ActorSystem()
  val factory = new ConnectionFactory()
  var connectionManagerActor: ActorRef = system.actorOf(Props[ConnectionManagerActor])
  val connection:ActorRef = system.actorOf(ConnectionActor.props(factory), "rabbitmq")
  val naming: ActorRef = system.actorOf(Props[RabbitMQNamingActor], "naming")
  val channelCreator: ActorRef = system.actorOf(Props[ChannelCreatorActor], "channelCreator")
  val publisher: ActorRef = system.actorOf(Props[PublisherActor], "publisher")
  //val collaborationMember: ActorRef = system.actorOf(Props(
  //  new CollaborationMembersActor(connection, naming, channelCreator, publisher)), "collaboration-members")
  //var getCollaborarionsActor:ActorRef = system.actorOf(Props.create(classOf[DBWorkerGetCollaborationActor], connectionManagerActor, collaborationMember))
  //var firebaseActor: ActorRef = system.actorOf(Props.create(classOf[FirebaseActor], getCollaborarionsActor))

  //firebaseActor ! PublishNotificationMessage("123456788698540008900400",UpdateMessage(UpdateMessageTarget.NOTE,UpdateMessageType.UPDATING,"maffone"))
}
