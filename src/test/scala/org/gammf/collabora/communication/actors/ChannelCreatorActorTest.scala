package org.gammf.collabora.communication.actors

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit}
import com.newmotion.akka.rabbitmq.{ConnectionActor, ConnectionFactory}
import org.gammf.collabora.TestUtil
import org.gammf.collabora.communication.messages.{ChannelCreatedMessage, PublishingChannelCreationMessage, SubscribingChannelCreationMessage}
import org.gammf.collabora.yellowpages.ActorCreator
import org.gammf.collabora.yellowpages.ActorService.ConnectionHandler
import org.gammf.collabora.yellowpages.actors.YellowPagesActor
import org.gammf.collabora.yellowpages.messages.{RegistrationRequestMessage, RegistrationResponseMessage}
import org.gammf.collabora.yellowpages.util.Topic
import org.gammf.collabora.yellowpages.TopicElement._
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

class ChannelCreatorActorTest extends TestKit (ActorSystem("CollaboraServer")) with WordSpecLike with Matchers with BeforeAndAfterAll with ImplicitSender{


    val CONNECTION_ACTOR_NAME = "RabbitConnection"
    val NAMING_ACTOR_NAME = "NamingActor"
    val CHANNEL_CREATOR_NAME = "RabbitChannelCreator"

    val actorCreator = new ActorCreator(system)
    val rootYellowPages = actorCreator.getYellowPagesRoot

    val factory = new ConnectionFactory()
    val rabbitConnection = system.actorOf(ConnectionActor.props(factory), CONNECTION_ACTOR_NAME)
    rootYellowPages ! RegistrationRequestMessage(rabbitConnection, CONNECTION_ACTOR_NAME, Topic() :+ Communication :+ RabbitMQ, ConnectionHandler)

    val channelCreator = system.actorOf(ChannelCreatorActor.printerProps(rootYellowPages, Topic() :+ Communication :+ RabbitMQ, CHANNEL_CREATOR_NAME))
    val namingActor = system.actorOf(RabbitMQNamingActor.printerProps(rootYellowPages, Topic() :+ Communication :+ RabbitMQ, NAMING_ACTOR_NAME))

    override def afterAll(): Unit = {
      TestKit.shutdownActorSystem(system)
    }

    "A ChannelCreator actor" must {

      "builds and returns to the sender a specific RabbitMQ channel created on the provided connection" in {
        channelCreator ! SubscribingChannelCreationMessage(TestUtil.TYPE_UPDATES, TestUtil.SERVER_UPDATE, None)
        expectMsgType[RegistrationResponseMessage]
        channelCreator ! PublishingChannelCreationMessage(TestUtil.TYPE_COLLABORATIONS, None)
        expectMsgType[ChannelCreatedMessage]
      }

    }
}
