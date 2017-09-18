package org.gammf.collabora.communication.actors

import org.gammf.collabora.communication.actors.rabbitmq.RabbitMQNotificationsSenderActor
import org.gammf.collabora.communication.messages.PublishNotificationMessage
import akka.actor.{ActorRef, Props}
import org.gammf.collabora.yellowpages.ActorService.ActorService
import org.gammf.collabora.yellowpages.actors.BasicActor
import org.gammf.collabora.yellowpages.util.Topic.ActorTopic
import org.gammf.collabora.yellowpages.util.Topic
import org.gammf.collabora.yellowpages.TopicElement._
import org.gammf.collabora.yellowpages.ActorService._

/**
  * This actor is responsible for forwarding certain messages to all the notification-related actors that exist in the server.
  */
class NotificationsDispatcherActor(override val yellowPages: ActorRef,
                                   override val name: String,
                                   override val topic: ActorTopic,
                                   override val service: ActorService = Bridging) extends BasicActor {
  override def receive: Receive = ({
    case updateMessage: PublishNotificationMessage =>
      self forward ForwardMessageToRabbitMQNotificationActorMessage(updateMessage)
      self forward ForwardMessageToFirebaseNotificationActorMessage(updateMessage)
    case forwardMessage: ForwardMessageToRabbitMQNotificationActorMessage =>
      getActorOrElse(Topic() :+ Communication :+ Notifications :+ RabbitMQ, Master, forwardMessage).
        foreach(_ forward forwardMessage.message)
    case forwardMessage: ForwardMessageToFirebaseNotificationActorMessage =>
      getActorOrElse(Topic() :+ Communication :+ Notifications :+ Firebase, Master, forwardMessage).
        foreach(_ forward forwardMessage.message)

  }: Receive) orElse super[BasicActor].receive
}

/**
  * Message used internally by a [[NotificationsDispatcherActor]] in order to manage message-forwarding to a [[RabbitMQNotificationsSenderActor]]
  *
  * @param message the message to be forwarded
  */
private case class ForwardMessageToRabbitMQNotificationActorMessage(message: PublishNotificationMessage)

/**
  * Message used internally by a [[NotificationsDispatcherActor]] in order to manage message-forwarding to a [[FirebaseActor]]
  *
  * @param message the message to be forwarded
  */
private case class ForwardMessageToFirebaseNotificationActorMessage(message: PublishNotificationMessage)


object NotificationsDispatcherActor {
  /**
    * Factory method that returns a Props to create an already-registered notifications dispatcher actor.
    * @param yellowPages the reference to the yellow pages root actor.
    * @param topic the topic to which this actor is going to be registered.
    * @return the Props to use to create a notifications dispatcher actor.
    */

  def notificationsDispatcherProps(yellowPages: ActorRef, topic: ActorTopic, name: String = "NotificationDispatcher") : Props =
    Props(new NotificationsDispatcherActor(yellowPages = yellowPages, name = name, topic = topic))
}
