package org.gammf.collabora.communication.actors.rabbitmq

import akka.actor.ActorRef
import org.gammf.collabora.communication.CommunicationType._
import org.gammf.collabora.communication.messages.{ChannelNamesRequestMessage, ChannelNamesResponseMessage}
import org.gammf.collabora.yellowpages.ActorService.ActorService
import org.gammf.collabora.yellowpages.actors.BasicActor
import org.gammf.collabora.yellowpages.util.Topic.ActorTopic

/**
  * This is an actor that handles the rabbitMQ naming issues.
  * Can reply to a names request with all the relevant information needed to build a rabbitMQ channel.
  */
class RabbitMQNamingActor(override val yellowPages: ActorRef, override val name: String,
                          override val topic: ActorTopic, override val service: ActorService) extends BasicActor {

  override def receive: Receive = ({
    case ChannelNamesRequestMessage(communicationType: CommunicationType) => sender ! buildNamesResponse(communicationType)
  }:Receive) orElse super[BasicActor].receive

  private[this] def buildNamesResponse(communicationType: CommunicationType): ChannelNamesResponseMessage = communicationType match {
    case UPDATES => ChannelNamesResponseMessage("updates", Some("update.server"))
    case NOTIFICATIONS => ChannelNamesResponseMessage("notifications", None)
    case COLLABORATIONS => ChannelNamesResponseMessage("collaborations", None)
    case _ => ChannelNamesResponseMessage("", None)
  }
}