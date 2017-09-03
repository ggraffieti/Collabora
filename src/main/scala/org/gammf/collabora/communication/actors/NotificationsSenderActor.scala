package org.gammf.collabora.communication.actors

import akka.actor.{Actor, ActorRef, ActorSystem, Props, Stash}
import com.newmotion.akka.rabbitmq.{Channel, ConnectionActor, ConnectionFactory}
import org.gammf.collabora.EntryPoint._
import org.gammf.collabora.communication.Utils.CommunicationType
import org.gammf.collabora.communication.messages._
import org.gammf.collabora.database.actors._
import org.gammf.collabora.util.{UpdateMessageTarget, UpdateMessageType}
import play.api.libs.json.Json

/**
  * @author Manuel Peruzzi
  */

/**
  * This is an actor that manages sending notifications to clients.
  * @param connection the open connection with the rabbitMQ broker.
  * @param naming the reference to a rabbitMQ naming actor.
  * @param channelCreator the reference to a channel creator actor.
  * @param publisher the reference to a publisher actor.
  * @param system the actor system.
  */
class NotificationsSenderActor(connection: ActorRef, naming: ActorRef, channelCreator: ActorRef,
                              publisher: ActorRef,system: ActorSystem) extends Actor with Stash {

  private[this] var pubChannel: Option[Channel] = None
  private[this] var pubExchange: Option[String] = None
  private var firebaseActor: ActorRef = _

  override def preStart(): Unit = {
   firebaseActor = system.actorOf(Props.create(classOf[FirebaseActor]))
  }


  override def receive: Receive = {
    case StartMessage => naming ! ChannelNamesRequestMessage(CommunicationType.NOTIFICATIONS)
    case ChannelNamesResponseMessage(exchange, _) =>
      pubExchange = Some(exchange)
      channelCreator ! PublishingChannelCreationMessage(connection, exchange, None)
    case ChannelCreatedMessage(channel) =>
      pubChannel = Some(channel)
      unstashAll()
    case PublishNotificationMessage(collaborationID, message) =>
      pubChannel match {
        case Some(channel) =>
          publisher ! PublishMessage(channel, pubExchange.get, Some(collaborationID), Json.toJson(message))
          message.target match {
            case UpdateMessageTarget.COLLABORATION => //do nothing
            case UpdateMessageTarget.MEMBER =>
              message.messageType match {
                case UpdateMessageType.CREATION => firebaseActor ! PublishNotificationMessage(collaborationID,message)
                case _=> //do nothing
              }
            case _=> firebaseActor ! PublishNotificationMessage(collaborationID,message)
          }
        case _ =>
          stash()
      }
    case _ => println("[NotificationsSenderActor] Huh?")
  }
}

/**
  * This is a simple application that uses the Updates Receiver Actor.
  */
object UseNotificationsSenderActor extends App {
  implicit val system = ActorSystem()
  val factory = new ConnectionFactory()
  val connection = system.actorOf(ConnectionActor.props(factory), "rabbitmq")

  val naming = system.actorOf(Props[RabbitMQNamingActor], "naming")
  val channelCreator = system.actorOf(Props[ChannelCreatorActor], "channelCreator")
  val publisher = system.actorOf(Props[PublisherActor], "publisher")
  val collaborationActor = system.actorOf(Props(new CollaborationMembersActor(connection, naming, channelCreator, publisher)))
  val notificationsSender = system.actorOf(Props(
    new NotificationsSenderActor(connection, naming, channelCreator, publisher,system)), "notifications-sender")

  Thread.sleep(1000)
  notificationsSender ! StartMessage
}