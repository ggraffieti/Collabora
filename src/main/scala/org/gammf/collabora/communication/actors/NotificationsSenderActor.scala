package org.gammf.collabora.communication.actors

import akka.actor.{ActorRef, ActorSystem, Props, Stash}
import com.newmotion.akka.rabbitmq.{Channel, ConnectionActor, ConnectionFactory}
import org.gammf.collabora.communication.Utils.CommunicationType
import org.gammf.collabora.communication.messages._
import org.gammf.collabora.yellowpages.ActorService.ActorService
import org.gammf.collabora.yellowpages.actors.BasicActor
import org.gammf.collabora.yellowpages.util.Topic.ActorTopic
import play.api.libs.json.Json
import org.gammf.collabora.yellowpages.util.Topic
import org.gammf.collabora.yellowpages.TopicElement._
import org.gammf.collabora.yellowpages.ActorService._
import org.gammf.collabora.yellowpages.messages.RegistrationResponseMessage

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
class NotificationsSenderActor(override val yellowPages: ActorRef, override val name: String,
                               override val topic: ActorTopic, override val service: ActorService) extends BasicActor with Stash {

  private[this] var pubChannel: Option[Channel] = None
  private[this] var pubExchange: Option[String] = None

  override def receive: Receive = ({
    case message: RegistrationResponseMessage => getActorOrElse(Topic() :+ Communication :+ RabbitMQ, Naming, message).foreach(_ ! ChannelNamesRequestMessage(CommunicationType.NOTIFICATIONS))
    case message: ChannelNamesResponseMessage =>
      pubExchange = Some(message.exchange)
      getActorOrElse(Topic() :+ Communication :+ RabbitMQ, ChannelCreating, message).foreach(_ ! PublishingChannelCreationMessage(message.exchange, None))
    case ChannelCreatedMessage(channel) =>
      pubChannel = Some(channel)
      unstashAll()
    case updateMessage: PublishNotificationMessage =>
      pubChannel match {
        case Some(channel) =>
          getActorOrElse(Topic() :+ Communication :+ RabbitMQ, Publishing, updateMessage).foreach(_ ! PublishMessage(channel, pubExchange.get, Some(updateMessage.collaborationID), Json.toJson(updateMessage.message)))
        case _ => stash()
      }
  }: Receive) orElse super[BasicActor].receive
}

/**
  * This is a simple application that uses the Updates Receiver Actor.
  */
object UseNotificationsSenderActor extends App {
  //TODO refactor
  implicit val system: ActorSystem = ActorSystem()
  val factory = new ConnectionFactory()
  val connection = system.actorOf(ConnectionActor.props(factory), "rabbitmq")

  val naming = system.actorOf(Props[RabbitMQNamingActor], "naming")
  val channelCreator = system.actorOf(Props[ChannelCreatorActor], "channelCreator")
  //val publisher = system.actorOf(Props[PublisherActor], "publisher")
  //val collaborationActor = system.actorOf(Props(new CollaborationMembersActor(connection, naming, channelCreator, publisher)))
  //val notificationsSender = system.actorOf(Props(
  //  new NotificationsSenderActor(connection, naming, channelCreator, publisher,system)), "notifications-sender")

  Thread.sleep(1000)
  //notificationsSender ! StartMessage
}