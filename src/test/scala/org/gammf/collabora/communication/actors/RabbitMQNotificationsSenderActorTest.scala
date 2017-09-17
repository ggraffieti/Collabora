package org.gammf.collabora.communication.actors

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import akka.testkit.{DefaultTimeout, ImplicitSender, TestKit}
import akka.pattern.ask
import akka.util.Timeout
import org.gammf.collabora.communication.messages.PublishMessage
import org.gammf.collabora.{TestMessageUtil, TestUtil}
import org.gammf.collabora.yellowpages.ActorContainer
import org.gammf.collabora.yellowpages.ActorService._
import org.gammf.collabora.yellowpages.messages._
import org.gammf.collabora.yellowpages.util.Topic
import org.gammf.collabora.yellowpages.TopicElement._
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

import scala.concurrent.duration._
import org.scalatest.concurrent.Eventually
import play.api.libs.json.Json

import scala.concurrent.Await
import scala.language.postfixOps


class RabbitMQNotificationsSenderActorTest extends TestKit (ActorSystem("CollaboraTest")) with WordSpecLike with Eventually with DefaultTimeout with Matchers with BeforeAndAfterAll with ImplicitSender {

  implicit protected[this] val askTimeout: Timeout = Timeout(5 second)
  override implicit val patienceConfig: PatienceConfig = PatienceConfig(
    timeout = scaled(TestUtil.TIMEOUT_SECOND seconds),
    interval = scaled(TestUtil.INTERVAL_MILLIS millis)
  )

  var rootYellowPages: ActorRef = _
  var expectedMessage: String = ""

  override def beforeAll(): Unit ={
    ActorContainer.init()
    ActorContainer.createAll()
    rootYellowPages = ActorContainer.rootYellowPages

    removePublisherActorFromYellowPages()
    subscribeFakePublisherActor()

    Thread.sleep(200)
  }

  override def afterAll(): Unit = {
    ActorContainer.shutdown()
    TestKit.shutdownActorSystem(system)
  }

  "A NotificationsSender actor" should {

    "correcly send notifications abount a note when needed" in {
      val message = TestMessageUtil.publishNoteNotificationMessage

      within(TestUtil.TASK_WAIT_TIME seconds){
        Await.result(rootYellowPages ? ActorRequestMessage(Topic() :+ Communication :+ Notifications :+ RabbitMQ, Master), askTimeout.duration)
          .asInstanceOf[ActorResponseMessage] match {
          case response:
            ActorResponseOKMessage => response.actor ! message
          case _ => fail
        }
        eventually {
          expectedMessage should not be ""
        }
        assert(expectedMessage == Json.toJson(TestMessageUtil.noteUpdateMessage).toString())
        expectedMessage = ""
      }
    }

    "correcly send notifications abount a module when needed" in {
      val message = TestMessageUtil.publishModuleNotificationMessage

      within(TestUtil.TASK_WAIT_TIME seconds){
        Await.result(rootYellowPages ? ActorRequestMessage(Topic() :+ Communication :+ Notifications :+ RabbitMQ, Master), askTimeout.duration)
          .asInstanceOf[ActorResponseMessage] match {
          case response:
            ActorResponseOKMessage => response.actor ! message
          case _ => fail
        }
        eventually {
          expectedMessage should not be ""
        }
        assert(expectedMessage == Json.toJson(TestMessageUtil.moduleUpdateMessage).toString())
        expectedMessage = ""
      }
    }

    "correcly send notifications abount a collaboration when needed" in {
      val message = TestMessageUtil.publishCollaborationNotificationMessage

      within(TestUtil.TASK_WAIT_TIME seconds){
        Await.result(rootYellowPages ? ActorRequestMessage(Topic() :+ Communication :+ Notifications :+ RabbitMQ, Master), askTimeout.duration)
          .asInstanceOf[ActorResponseMessage] match {
          case response:
            ActorResponseOKMessage => response.actor ! message
          case _ => fail
        }
        eventually {
          expectedMessage should not be ""
        }
        assert(expectedMessage == Json.toJson(TestMessageUtil.collaborationUpdateMessage).toString())
        expectedMessage = ""
      }
    }

    "correcly send notifications abount a member when needed" in {
      val message = TestMessageUtil.publishMemberNotificationMessage

      within(TestUtil.TASK_WAIT_TIME seconds){
        Await.result(rootYellowPages ? ActorRequestMessage(Topic() :+ Communication :+ Notifications :+ RabbitMQ, Master), askTimeout.duration)
          .asInstanceOf[ActorResponseMessage] match {
          case response:
            ActorResponseOKMessage => response.actor ! message
          case _ => fail
        }
        eventually {
          expectedMessage should not be ""
        }
        assert(expectedMessage == Json.toJson(TestMessageUtil.memberUpdateMessage).toString())
      }
    }
  }

  private def removePublisherActorFromYellowPages(): Unit = {
    Thread.sleep(200)
    Await.result(rootYellowPages ? ActorRequestMessage(Topic() :+ Communication :+ RabbitMQ, Publishing), askTimeout.duration)
      .asInstanceOf[ActorResponseMessage] match {
      case response: ActorResponseOKMessage => rootYellowPages ! DeletionRequestMessage(response.actor, "PublisherActor", Topic() :+ Communication :+ RabbitMQ, Publishing)
      case _ => fail
    }
  }


  private def subscribeFakePublisherActor(): Unit = {
    val fakePublisherActor = ActorContainer.actorSystem.actorOf(Props(new Actor {
      override def receive: Receive = {
        case publishMessage: PublishMessage => expectedMessage = publishMessage.message.toString()
      }
    }))
    rootYellowPages ! RegistrationRequestMessage(fakePublisherActor, "fakePublisherActor", Topic() :+ Communication :+ RabbitMQ, Publishing)
  }
}
