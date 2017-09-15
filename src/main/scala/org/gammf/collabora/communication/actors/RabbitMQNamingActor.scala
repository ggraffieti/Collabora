package org.gammf.collabora.communication.actors

import akka.actor.{ActorRef, Props}
import org.gammf.collabora.communication.Utils.CommunicationType
import org.gammf.collabora.communication.messages.{ChannelNamesRequestMessage, ChannelNamesResponseMessage}
import org.gammf.collabora.yellowpages.ActorService.{ActorService, Naming}
import org.gammf.collabora.yellowpages.actors.BasicActor
import org.gammf.collabora.yellowpages.util.Topic.ActorTopic

/**
  * This is an actor that handles the rabbitMQ naming issues.
  */
class RabbitMQNamingActor(override val yellowPages: ActorRef,
                          override val name: String,
                          override val topic: ActorTopic,
                          override val service: ActorService = Naming) extends BasicActor {

  override def receive: Receive = ({
    case ChannelNamesRequestMessage(commType: CommunicationType.Value) => sender ! names2Message(commType)
  }:Receive) orElse super[BasicActor].receive

  private[this] implicit def type2Names(commType: CommunicationType.Value): CommunicationNames =
    commType match {
      case CommunicationType.UPDATES => UpdatesNames()
      case CommunicationType.NOTIFICATIONS => NotificationsNames()
      case CommunicationType.COLLABORATIONS => CollaborationsNames()
    }

  private[this] def names2Message(commNames: CommunicationNames): ChannelNamesResponseMessage = {
    ChannelNamesResponseMessage(commNames.exchange, commNames.queue)
  }
}

private sealed trait CommunicationNames {
  def exchange: String = this match {
    case _:UpdatesNames => "updates"
    case _:NotificationsNames => "notifications"
    case _:CollaborationsNames => "collaborations"
    case _ => ""
  }

  def queue: Option[String] = this match {
    case _:UpdatesNames => Some("update.server")
    case _ => None
  }
}

private case class UpdatesNames() extends CommunicationNames
private case class NotificationsNames() extends CommunicationNames
private case class CollaborationsNames() extends CommunicationNames



object RabbitMQNamingActor {

  /**
    * Factory methods that return a [[Props]] to create a RabbitMQ Naming registered actor
    * @param yellowPages the reference to the yellow pages root actor.
    * @param topic the topic to which this actor is going to be registered.
    * @return the [[Props]] to use to create a RabbitMQ Naming actor.
    */

  def namingProps(yellowPages: ActorRef, topic: ActorTopic, name: String = "NamingActor") : Props =
    Props(new RabbitMQNamingActor(yellowPages = yellowPages, name = name, topic = topic))
}








