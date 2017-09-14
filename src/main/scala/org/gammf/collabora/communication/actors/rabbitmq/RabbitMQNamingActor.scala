package org.gammf.collabora.communication.actors.rabbitmq

import akka.actor.ActorRef
import org.gammf.collabora.communication.Utils.CommunicationType
import org.gammf.collabora.communication.messages.{ChannelNamesRequestMessage, ChannelNamesResponseMessage}
import org.gammf.collabora.yellowpages.ActorService.ActorService
import org.gammf.collabora.yellowpages.actors.BasicActor
import org.gammf.collabora.yellowpages.util.Topic.ActorTopic

import scala.language.implicitConversions

/**
  * This is an actor that handles the rabbitMQ naming issues.
  */
class RabbitMQNamingActor(override val yellowPages: ActorRef, override val name: String,
                          override val topic: ActorTopic, override val service: ActorService) extends BasicActor {

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