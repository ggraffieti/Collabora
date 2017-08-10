package org.gammf.collabora.communication.actors

import akka.actor.Actor
import org.gammf.collabora.communication.messages.{ChannelNamesRequestMessage, ChannelNamesResponseMessage}

/**
  * @author Manuel Peruzzi
  */

/**
  * This is an actor that handles the rabbitMQ naming issues.
  */
class RabbitMQNamingActor extends Actor {
  implicit def type2Names(commType: CommunicationType.Value): CommunicationNames =
    commType match {
      case CommunicationType.UPDATES => UpdatesNames()
      case CommunicationType.NOTIFICATIONS => NotificationsNames()
      case CommunicationType.COLLABORATIONS => CollaborationsNames()
    }

  override def receive: Receive = {
    case ChannelNamesRequestMessage(commType: CommunicationType.Value) => sender ! names2Message(commType)
    case _ => println("Huh?")
  }

  private def names2Message(commNames: CommunicationNames): ChannelNamesResponseMessage = {
    ChannelNamesResponseMessage(commNames.exchange, commNames.queue)
  }
}

/**
  * This is a simple enumeration containing the types of the client-server communication.
  */
object CommunicationType extends Enumeration {
  val UPDATES, NOTIFICATIONS, COLLABORATIONS = Value
}

protected sealed trait CommunicationNames {
  val exchange: String = this match {
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