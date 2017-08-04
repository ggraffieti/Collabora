package org.gammf.collabora.communication

import akka.actor.Actor

/**
  * Created by mperuzzi on 04/08/17.
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
    ChannelNamesResponseMessage(commNames.exchange, commNames.queue, commNames.routingKey)
  }
}

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

  def routingKey: Option[String] = None
}

private case class UpdatesNames() extends CommunicationNames
private case class NotificationsNames() extends CommunicationNames
private case class CollaborationsNames() extends CommunicationNames