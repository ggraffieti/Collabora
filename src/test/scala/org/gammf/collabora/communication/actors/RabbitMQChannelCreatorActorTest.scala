package org.gammf.collabora.communication.actors

import akka.actor.{ActorRef, ActorSystem}
import akka.pattern.ask
import akka.testkit.{ImplicitSender, TestKit}
import akka.util.Timeout
import org.gammf.collabora.TestUtil
import org.gammf.collabora.communication.messages.{ChannelCreatedMessage, PublishingChannelCreationMessage, SubscribingChannelCreationMessage}
import org.gammf.collabora.yellowpages.ActorContainer
import org.gammf.collabora.yellowpages.ActorService.ChannelCreating
import org.gammf.collabora.yellowpages.messages._
import org.gammf.collabora.yellowpages.util.Topic
import org.gammf.collabora.yellowpages.TopicElement._
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.language.postfixOps

class RabbitMQChannelCreatorActorTest extends TestKit (ActorSystem("CollaboraTest")) with WordSpecLike with Matchers with BeforeAndAfterAll with ImplicitSender{

  implicit protected[this] val askTimeout: Timeout = Timeout(5 second)

  var rootYellowPages: ActorRef = _

  override def beforeAll(): Unit = {
    ActorContainer.init()
    ActorContainer.createAll()
    rootYellowPages = ActorContainer.rootYellowPages
    Thread.sleep(200)
  }


  override def afterAll(): Unit = {
    ActorContainer.shutdown()
    TestKit.shutdownActorSystem(system)
  }

  "A ChannelCreator actor" must {

    "builds and returns to the sender a specific RabbitMQ channel created on the provided connection" in {

      Await.result(rootYellowPages ? ActorRequestMessage(Topic() :+ Communication :+ RabbitMQ, ChannelCreating), askTimeout.duration) match {
        case response: ActorResponseOKMessage =>
          response.actor ! SubscribingChannelCreationMessage(TestUtil.TYPE_UPDATES, TestUtil.SERVER_UPDATE, None)
          expectMsgType[ChannelCreatedMessage]
        case _ => fail

      }

      Await.result(rootYellowPages ? ActorRequestMessage(Topic() :+ Communication :+ RabbitMQ, ChannelCreating), askTimeout.duration) match {
        case response: ActorResponseOKMessage =>
          response.actor ! PublishingChannelCreationMessage(TestUtil.TYPE_COLLABORATIONS, None)
          expectMsgType[ChannelCreatedMessage]
        case _ => fail
      }
    }
  }
}
