package org.gammf.collabora.communication.actors

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit}
import com.newmotion.akka.rabbitmq.{ConnectionActor, ConnectionFactory}
import org.gammf.collabora.communication.messages.{ChannelCreatedMessage, PublishingChannelCreationMessage, SubscribingChannelCreationMessage}
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

class ChannelCreatorActorTest extends TestKit (ActorSystem("CollaboraServer")) with WordSpecLike with Matchers with BeforeAndAfterAll with ImplicitSender{

    val CONNECTION_ACTOR_NAME = "rabbitmq"
    val NAMING_ACTOR_NAME = "naming"
    val CHANNEL_CREATOR_NAME = "channelCreator"

    val factory = new ConnectionFactory()
    val connection:ActorRef = system.actorOf(ConnectionActor.props(factory), CONNECTION_ACTOR_NAME)
    val naming: ActorRef = system.actorOf(Props[RabbitMQNamingActor], NAMING_ACTOR_NAME)
    val channelCreator: ActorRef = system.actorOf(Props[ChannelCreatorActor], CHANNEL_CREATOR_NAME)

    override def afterAll(): Unit = {
      TestKit.shutdownActorSystem(system)
    }

    "A ChannelCreator actor" must {

      "builds and returns to the sender a specific RabbitMQ channel created on the provided connection" in {
        channelCreator ! SubscribingChannelCreationMessage(connection, CommunicationTestUtil.TYPE_UPDATES, CommunicationTestUtil.SERVER_UPDATE, None)
        expectMsgType[ChannelCreatedMessage]
        channelCreator ! PublishingChannelCreationMessage(connection, CommunicationTestUtil.TYPE_COLLABORATIONS, None)
        expectMsgType[ChannelCreatedMessage]
      }

    }
}
