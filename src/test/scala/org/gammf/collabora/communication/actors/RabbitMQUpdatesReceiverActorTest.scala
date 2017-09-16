package org.gammf.collabora.communication.actors

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.pattern.ask
import akka.testkit.{ImplicitSender, TestKit}
import akka.util.Timeout
import org.gammf.collabora.communication.CommunicationType
import org.gammf.collabora.TestUtil
import org.gammf.collabora.communication.messages._
import org.gammf.collabora.database.messages.GetConnectionMessage
import org.gammf.collabora.yellowpages.ActorContainer
import org.gammf.collabora.yellowpages.ActorService.{ChannelCreating, Naming}
import org.gammf.collabora.yellowpages.messages._
import org.gammf.collabora.yellowpages.util.Topic
import org.gammf.collabora.yellowpages.TopicElement._
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.language.postfixOps

class RabbitMQUpdatesReceiverActorTest extends TestKit (ActorSystem("CollaboraTest")) with WordSpecLike  with Matchers with BeforeAndAfterAll with ImplicitSender {

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

  "A UpdatesReceiver actor" should {

    "start correctly" in {
      within(TestUtil.TASK_WAIT_TIME seconds){
        (rootYellowPages ? ActorRequestMessage(Topic() :+ Communication :+ RabbitMQ, Naming))
          .mapTo[ActorResponseMessage].map {
          case response: ActorResponseOKMessage => response.actor ! ChannelNamesRequestMessage(CommunicationType.UPDATES)
          case _ =>

            expectMsg(RegistrationResponseMessage())
        }
      }
    }

    "create channel correctly" in {
      within(TestUtil.TASK_WAIT_TIME seconds){
        (rootYellowPages ? ActorRequestMessage(Topic() :+ Communication :+ RabbitMQ, ChannelCreating))
          .mapTo[ActorResponseMessage].map {
          case response: ActorResponseOKMessage =>
            response.actor ! SubscribingChannelCreationMessage(TestUtil.TYPE_UPDATES, TestUtil.SERVER_UPDATE, None)
            expectMsgType[GetConnectionMessage]
          case _ =>
        }
      }
    }
  }
}

