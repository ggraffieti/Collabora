package org.gammf.collabora.database.actors

import akka.actor.{ActorRef, ActorSystem}
import akka.testkit.{ImplicitSender, TestKit}
import akka.util.Timeout
import akka.pattern.ask
import org.gammf.collabora.TestMessageUtil
import org.gammf.collabora.database.messages.{CheckMemberExistenceRequestMessage, CheckMemberExistenceResponseMessage, QueryOkMessage}
import org.gammf.collabora.yellowpages.ActorContainer
import org.gammf.collabora.yellowpages.ActorService._
import org.gammf.collabora.yellowpages.messages.{ActorRequestMessage, ActorResponseMessage, ActorResponseOKMessage}
import org.gammf.collabora.yellowpages.util.Topic
import org.gammf.collabora.yellowpages.TopicElement._

import scala.concurrent.duration._
import org.scalatest.concurrent.Eventually
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

import scala.concurrent.Await
import scala.language.postfixOps

class DBWorkerCheckMemberExistenceActorTest extends TestKit (ActorSystem("CollaboraTest")) with WordSpecLike with Eventually with Matchers with BeforeAndAfterAll with ImplicitSender {

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

  "A DBWorkerCheckMemberExistence" should {
    "respond with a \"yes, the member exists\"" in {
      Await.result(rootYellowPages ? ActorRequestMessage(Topic() :+ Database :+ Member, ExistenceChecking), askTimeout.duration)
        .asInstanceOf[ActorResponseMessage] match {
        case response: ActorResponseOKMessage =>
          response.actor ! CheckMemberExistenceRequestMessage("maffone")
          expectMsgPF() {
            case QueryOkMessage(CheckMemberExistenceResponseMessage(username, existence)) => assert(existence && username == "maffone")
          }
        case _ => fail
      }
    }
    "responde with a \"no, the member does not exist\"" in {
      Await.result(rootYellowPages ? ActorRequestMessage(Topic() :+ Database :+ Member, ExistenceChecking), askTimeout.duration)
        .asInstanceOf[ActorResponseMessage] match {
        case response: ActorResponseOKMessage =>
          response.actor ! CheckMemberExistenceRequestMessage("usernameNotPresent")
          expectMsgPF() {
            case QueryOkMessage(CheckMemberExistenceResponseMessage(username, existence)) => assert(!existence && username == "usernameNotPresent")
          }
        case _ => fail
      }
    }
  }
}
