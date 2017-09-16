package org.gammf.collabora.communication.actors.rabbitmq

import akka.actor.{ActorRef, Props}
import org.gammf.collabora.communication.CommunicationType._
import org.gammf.collabora.communication.messages.{ChannelNamesRequestMessage, ChannelNamesResponseMessage}
import org.gammf.collabora.yellowpages.ActorService.ActorService
import org.gammf.collabora.yellowpages.actors.BasicActor
import org.gammf.collabora.yellowpages.util.Topic.ActorTopic
import org.gammf.collabora.yellowpages.ActorService._

/**
  * This is an actor that handles the rabbitMQ naming issues.
  * Can reply to a names request with all the relevant information needed to build a rabbitMQ channel.
  */
class RabbitMQNamingActor(override val yellowPages: ActorRef, override val name: String,
                          override val topic: ActorTopic, override val service: ActorService = Naming) extends BasicActor {

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