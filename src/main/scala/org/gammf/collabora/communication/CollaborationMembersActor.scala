package org.gammf.collabora.communication

import akka.actor.{Actor, ActorRef, Stash}
import com.newmotion.akka.rabbitmq.Channel

/**
  * @author Manuel Peruzzi
  */

/**
  * This is an actor that sends all the information needed by a user that has just been added to a collaboration.
  * @param connection the open connection with the rabbitMQ broker.
  * @param naming the reference to a rabbitMQ naming actor.
  * @param channelCreator the reference to a channel creator actor.
  * @param publisher the reference to a publisher actor.
  */
class CollaborationMembersActor(connection: ActorRef, naming: ActorRef, channelCreator: ActorRef,
                                publisher: ActorRef) extends Actor with Stash {

  private var pubChannel: Option[Channel] = None
  private var pubExchange: Option[String] = None

  override def receive: Receive = {
    case StartMessage => naming ! ChannelNamesRequestMessage(CommunicationType.COLLABORATIONS)
    case ChannelNamesResponseMessage(exchange, _) =>
      pubExchange = Some(exchange)
      channelCreator ! PublishingChannelCreationMessage(connection, exchange, None)
    case ChannelCreatedMessage(channel) =>
      pubChannel = Some(channel)
      unstashAll()
    case PublishMemberAddedMessage(username, message) =>
      pubChannel match {
        case Some(channel) =>
          publisher ! PublishMessage(channel, pubExchange.get, Some(username), message.toString())
        case _ => stash()
      }

    case _ => println("[CollaborationMembersActor] Huh?")
  }

}
