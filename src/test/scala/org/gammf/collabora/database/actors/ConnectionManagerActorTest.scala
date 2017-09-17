package org.gammf.collabora.database.actors

import akka.actor.{ActorRef, ActorSystem}
import akka.pattern.ask
import akka.testkit.{ImplicitSender, TestKit}
import akka.util.Timeout
import org.gammf.collabora.TestUtil
import org.gammf.collabora.database.messages.{AskConnectionMessage, GetConnectionMessage}
import org.gammf.collabora.yellowpages.ActorContainer
import org.gammf.collabora.yellowpages.ActorService.ConnectionHandler
import org.gammf.collabora.yellowpages.messages._
import org.gammf.collabora.yellowpages.TopicElement._
import org.gammf.collabora.yellowpages.util.Topic
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.language.postfixOps

class ConnectionManagerActorTest extends TestKit (ActorSystem("CollaboraTest")) with WordSpecLike  with Matchers with BeforeAndAfterAll with ImplicitSender {

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

  "A ConnectionManager actor" should {

    "send back connection messages correctly" in {
      within(TestUtil.TASK_WAIT_TIME seconds) {
        Await.result(rootYellowPages ? ActorRequestMessage(Topic() :+ Database, ConnectionHandler), askTimeout.duration)
          .asInstanceOf[ActorResponseMessage] match {
          case response: ActorResponseOKMessage =>
            response.actor ! AskConnectionMessage()
            expectMsgType[GetConnectionMessage]
          case _ => fail
        }
      }
    }
  }

}
