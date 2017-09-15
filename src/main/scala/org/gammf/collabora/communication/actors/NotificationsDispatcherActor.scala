package org.gammf.collabora.communication.actors

import akka.actor.{ActorRef, Props}
import org.gammf.collabora.communication.messages.{ForwardMessageToFirebaseNotificationActor, ForwardMessageToRabbitMQNotificationActor, PublishNotificationMessage}
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
      self forward ForwardMessageToRabbitMQNotificationActor(updateMessage)
      self forward ForwardMessageToFirebaseNotificationActor(updateMessage)
    case forwardMessage: ForwardMessageToRabbitMQNotificationActor =>
      getActorOrElse(Topic() :+ Communication :+ Notifications :+ RabbitMQ, Master, forwardMessage).
        foreach(_ forward forwardMessage.message)
    case forwardMessage: ForwardMessageToFirebaseNotificationActor =>
      getActorOrElse(Topic() :+ Communication :+ Notifications :+ Firebase, Master, forwardMessage).
        foreach(_ forward forwardMessage.message)

  }: Receive) orElse super[BasicActor].receive
}

object NotificationsDispatcherActor {
  /**
    * Factory methods that return a [[Props]] to create a notifications dispatcher registered actor
    * @param yellowPages the reference to the yellow pages root actor.
    * @param topic the topic to which this actor is going to be registered.
    * @return the [[Props]] to use to create a notifications dispatcher actor.
    */

  def notificationsDispatcherProps(yellowPages: ActorRef, topic: ActorTopic, name: String = "NotificationDispatcher") : Props =
    Props(new NotificationsDispatcherActor(yellowPages = yellowPages, name = name, topic = topic))
}
