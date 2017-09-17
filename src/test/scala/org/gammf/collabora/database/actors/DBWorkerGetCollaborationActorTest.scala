package org.gammf.collabora.database.actors

import akka.actor.{ActorRef, ActorSystem}
import akka.testkit.{ImplicitSender, TestKit}
import akka.util.Timeout
import akka.pattern.ask
import org.gammf.collabora.TestMessageUtil
import org.gammf.collabora.database.messages.{GetAllCollaborationsMessage, GetCollaborationMessage}
import org.gammf.collabora.util.Collaboration

import scala.concurrent.duration._
import org.scalatest.concurrent.Eventually
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}
import org.gammf.collabora.yellowpages.{ActorContainer, TopicElement}
import org.gammf.collabora.yellowpages.ActorService._
import org.gammf.collabora.yellowpages.messages._
import org.gammf.collabora.yellowpages.util.Topic
import org.gammf.collabora.yellowpages.TopicElement._

import scala.concurrent.Await
import scala.language.postfixOps

class DBWorkerGetCollaborationActorTest extends TestKit (ActorSystem("CollaboraTest")) with WordSpecLike with Eventually with Matchers with BeforeAndAfterAll with ImplicitSender {

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

  "A DBWorkerGetCollaboration Actor" should {
    "return a well-formed collaboration" in {
      Await.result(rootYellowPages ? ActorRequestMessage(Topic() :+ Database :+ TopicElement.Collaboration, Getter), askTimeout.duration)
        .asInstanceOf[ActorResponseMessage] match {
        case response: ActorResponseOKMessage =>
          response.actor ! GetCollaborationMessage(TestMessageUtil.collaborationGetterCollaborationID)
          expectMsgPF() {
            case Some(h :: Nil) => assert(h.isInstanceOf[Collaboration])
            case None => fail
          }
        case _ => fail
      }
    }
    "return no collaborations" in {
      Await.result(rootYellowPages ? ActorRequestMessage(Topic() :+ Database :+ TopicElement.Collaboration, Getter), askTimeout.duration)
        .asInstanceOf[ActorResponseMessage] match {
        case response: ActorResponseOKMessage =>
          response.actor ! GetCollaborationMessage(TestMessageUtil.fakeCollaborationId)
          expectMsgPF() {
            case Some(_ :: Nil) => fail
            case None => succeed
          }
        case _ => fail
      }
    }
    "return all the collaborations" in {
      Await.result(rootYellowPages ? ActorRequestMessage(Topic() :+ Database :+ TopicElement.Collaboration, Getter), askTimeout.duration)
        .asInstanceOf[ActorResponseMessage] match {
        case response: ActorResponseOKMessage =>
          response.actor ! GetAllCollaborationsMessage("maffone")
          expectMsgPF() {
            case message: Option[List[Collaboration]] => assert(message.isDefined && message.get.size == 2)
            case None => fail
          }
        case _ => fail
      }
    }
  }
}
