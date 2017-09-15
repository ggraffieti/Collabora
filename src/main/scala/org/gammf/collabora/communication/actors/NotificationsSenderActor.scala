package org.gammf.collabora.communication.actors

import akka.actor.{ActorRef, Props, Stash}
import com.newmotion.akka.rabbitmq.Channel
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
  * This is an actor that manages sending notifications to clients.
  */
class NotificationsSenderActor(override val yellowPages: ActorRef,
                               override val name: String,
                               override val topic: ActorTopic,
                               override val service: ActorService = Master) extends BasicActor with Stash {

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

object NotificationsSenderActor {

  /**
    * Factory methods that return a [[Props]] to create a notifications sender registered actor
    * @param yellowPages the reference to the yellow pages root actor.
    * @param topic the topic to which this actor is going to be registered.
    * @return the [[Props]] to use to create a notifications sender actor.
    */

  def notificationsSenderProps(yellowPages: ActorRef, topic: ActorTopic, name: String = "NotificationActor") : Props =
    Props(new NotificationsSenderActor(yellowPages = yellowPages, name = name, topic = topic))
}