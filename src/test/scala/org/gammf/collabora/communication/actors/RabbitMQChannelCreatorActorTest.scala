package org.gammf.collabora.communication.actors

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit}
import com.newmotion.akka.rabbitmq.{ConnectionActor, ConnectionFactory}
import org.gammf.collabora.communication.messages.{ChannelCreatedMessage, PublishingChannelCreationMessage, SubscribingChannelCreationMessage}
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

class RabbitMQChannelCreatorActorTest extends TestKit (ActorSystem("CollaboraServer")) with WordSpecLike with Matchers with BeforeAndAfterAll with ImplicitSender{

    /*val factory = new ConnectionFactory()
    val connection:ActorRef = system.actorOf(ConnectionActor.props(factory), "rabbitmq")
    val naming: ActorRef = system.actorOf(Props[RabbitMQNamingActor], "naming")
    val channelCreator: ActorRef = system.actorOf(Props[ChannelCreatorActor], "channelCreator")

    override def afterAll(): Unit = {
      TestKit.shutdownActorSystem(system)
    }

    "A ChannelCreator actor" must {

      "builds and returns to the sender a specific RabbitMQ channel created on the provided connection" in {
        channelCreator ! SubscribingChannelCreationMessage(connection, "updates", "update.server", None)
        expectMsgType[ChannelCreatedMessage]
        channelCreator ! PublishingChannelCreationMessage(connection, "collaborations", None)
        expectMsgType[ChannelCreatedMessage]
      }

    }*/
}
