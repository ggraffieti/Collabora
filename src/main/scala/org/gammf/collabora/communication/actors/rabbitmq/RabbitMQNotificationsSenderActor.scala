package org.gammf.collabora.communication.actors.rabbitmq

import akka.actor.{ActorRef, Props, Stash}
import com.newmotion.akka.rabbitmq.Channel
import org.gammf.collabora.communication.CommunicationType
import org.gammf.collabora.communication.messages._
import org.gammf.collabora.yellowpages.ActorService.{ActorService, _}
import org.gammf.collabora.yellowpages.TopicElement._
import org.gammf.collabora.yellowpages.actors.BasicActor
import org.gammf.collabora.yellowpages.messages.RegistrationResponseMessage
import org.gammf.collabora.yellowpages.util.Topic
import org.gammf.collabora.yellowpages.util.Topic.ActorTopic
import play.api.libs.json.Json

/**
  * This actor completely manages the whole message sending to the rabbitMQ exchange Notifications.
  * Needs a publishing channel in order to reach the exchange, so all the publish message have to be stashed until this actor
  * acquires a valid channel.
  * After that, it can be used to send some information directly to all the members of a collaborations.
  */
class RabbitMQNotificationsSenderActor(override val yellowPages: ActorRef, override val name: String,
                                       override val topic: ActorTopic, override val service: ActorService = Master) extends BasicActor with Stash {

  private[this] var pubChannel: Option[Channel] = None
  private[this] var pubExchange: Option[String] = None

  override def receive: Receive = ({
    case message: RegistrationResponseMessage =>
      getActorOrElse(Topic() :+ Communication :+ RabbitMQ, Naming, message).foreach(_ ! ChannelNamesRequestMessage(CommunicationType.NOTIFICATIONS))
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

object RabbitMQNotificationsSenderActor {

  /**
    * Factory methods that return a [[Props]] to create a notifications sender registered actor
    * @param yellowPages the reference to the yellow pages root actor.
    * @param topic the topic to which this actor is going to be registered.
    * @return the [[Props]] to use to create a notifications sender actor.
    */

  def notificationsSenderProps(yellowPages: ActorRef, topic: ActorTopic, name: String = "NotificationActor") : Props =
    Props(new RabbitMQNotificationsSenderActor(yellowPages = yellowPages, name = name, topic = topic))
}
