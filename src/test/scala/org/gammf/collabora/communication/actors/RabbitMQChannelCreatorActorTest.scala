package org.gammf.collabora.communication.actors

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.pattern.ask
import akka.testkit.{ImplicitSender, TestKit}
import akka.util.Timeout
import org.gammf.collabora.yellowpages.entriesImplicitConversions._
import com.newmotion.akka.rabbitmq.{ConnectionActor, ConnectionFactory}
import org.gammf.collabora.TestUtil
import org.gammf.collabora.communication.messages.{ChannelCreatedMessage, PublishingChannelCreationMessage, SubscribingChannelCreationMessage}
import org.gammf.collabora.yellowpages.ActorCreator
import org.gammf.collabora.yellowpages.ActorService.{ChannelCreating, ConnectionHandler}
import org.gammf.collabora.yellowpages.actors.YellowPagesActor
import org.gammf.collabora.yellowpages.messages._
import org.gammf.collabora.yellowpages.util.Topic
import org.gammf.collabora.yellowpages.TopicElement._
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

class RabbitMQChannelCreatorActorTest extends TestKit (ActorSystem("CollaboraServer")) with WordSpecLike with Matchers with BeforeAndAfterAll with ImplicitSender{

    implicit protected[this] val askTimeout: Timeout = Timeout(5 second)

    val actorCreator = new ActorCreator(system)
    actorCreator.startCreation
    val rootYellowPages = actorCreator.getYellowPagesRoot

    override def afterAll(): Unit = {
      TestKit.shutdownActorSystem(system)
    }

    "A ChannelCreator actor" must {

      "builds and returns to the sender a specific RabbitMQ channel created on the provided connection" in {

        (rootYellowPages ? ActorRequestMessage(Topic() :+ Communication :+ RabbitMQ, ChannelCreating))
          .mapTo[ActorResponseMessage].map {
          case response: ActorResponseOKMessage => response.actor ! SubscribingChannelCreationMessage(TestUtil.TYPE_UPDATES, TestUtil.SERVER_UPDATE, None)
          case _ =>

           expectMsgType[RegistrationResponseMessage]
        }

        (rootYellowPages ? ActorRequestMessage(Topic() :+ Communication :+ RabbitMQ, ChannelCreating))
          .mapTo[ActorResponseMessage].map {
          case response: ActorResponseOKMessage => response.actor ! PublishingChannelCreationMessage(TestUtil.TYPE_COLLABORATIONS, None)
          case _ =>

            expectMsgType[ChannelCreatedMessage]
        }

      }

    }
}
