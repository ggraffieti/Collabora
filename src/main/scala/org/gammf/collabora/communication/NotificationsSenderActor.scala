package org.gammf.collabora.communication

import akka.actor.{Actor, ActorRef, ActorSystem, Props, Stash}
import com.newmotion.akka.rabbitmq.{Channel, ConnectionActor, ConnectionFactory}

/**
  * Created by mperuzzi on 07/08/17.
  */
class NotificationsSenderActor(connection: ActorRef, naming: ActorRef, channelCreator: ActorRef,
                              publisher: ActorRef) extends Actor with Stash {

  private var pubChannel: Option[Channel] = None
  private var pubExchange: Option[String] = None

  override def receive: Receive = {
    case StartMessage => naming ! ChannelNamesRequestMessage(CommunicationType.NOTIFICATIONS)
    case ChannelNamesResponseMessage(exchange, _) =>
      pubExchange = Some(exchange)
      channelCreator ! PublishingChannelCreationMessage(connection, exchange, None)
    case ChannelCreatedMessage(channel) =>
      pubChannel = Some(channel)
      unstashAll()
    case NotificationMessage(collaborationID, message) =>
      pubChannel match {
        case Some(channel) =>
          publisher ! PublishMessage(channel, pubExchange.get, Some(collaborationID), message)
        case _ =>
          stash()
      }

    case _ => println("[NotificationsSenderActor] Huh?")
  }
}

object UseNotificationsSenderActor extends App {
  implicit val system = ActorSystem()
  val factory = new ConnectionFactory()
  val connection = system.actorOf(ConnectionActor.props(factory), "rabbitmq")

  val naming = system.actorOf(Props[RabbitMQNamingActor], "naming")
  val channelCreator = system.actorOf(Props[ChannelCreatorActor], "channelCreator")
  val publisher = system.actorOf(Props[PublisherActor], "publisher")
  val notificationsSender = system.actorOf(Props(
    new NotificationsSenderActor(connection, naming, channelCreator, publisher)), "notifications-sender")

  notificationsSender ! NotificationMessage("collaborationID1", "Some simple text")
  Thread.sleep(1000)
  notificationsSender ! StartMessage
}