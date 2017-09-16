package org.gammf.collabora.database.actors

import akka.actor.{ActorSystem}
import akka.pattern.ask
import akka.testkit.{ImplicitSender, TestKit}
import akka.util.Timeout
import org.gammf.collabora.TestUtil
import org.gammf.collabora.database.messages.{AskConnectionMessage}
import org.gammf.collabora.yellowpages.ActorCreator
import org.gammf.collabora.yellowpages.ActorService.{ConnectionHandler}
import org.gammf.collabora.yellowpages.messages._
import org.gammf.collabora.yellowpages.TopicElement._
import org.gammf.collabora.yellowpages.util.Topic
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

class ConnectionManagerActorTest extends TestKit (ActorSystem("CollaboraServer")) with WordSpecLike  with Matchers with BeforeAndAfterAll with ImplicitSender {

  implicit protected[this] val askTimeout: Timeout = Timeout(5 second)
  val actorCreator = new ActorCreator(system)
  actorCreator.startCreation
  val rootYellowPages = actorCreator.getYellowPagesRoot

  override def afterAll(): Unit = {
    TestKit.shutdownActorSystem(system)
  }

  "A ConnectionManager actor" should {

    "send back connection message correctly" in {
      within(TestUtil.TASK_WAIT_TIME seconds) {
        (rootYellowPages ? ActorRequestMessage(Topic() :+ Database, ConnectionHandler))
          .mapTo[ActorResponseMessage].map {
          case response: ActorResponseOKMessage => response.actor ! new AskConnectionMessage()
          case _ =>

          expectMsgType[RegistrationResponseMessage]
        }
      }
    }
  }

}
