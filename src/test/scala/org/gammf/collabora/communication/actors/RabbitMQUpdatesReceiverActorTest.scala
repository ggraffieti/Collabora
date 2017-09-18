package org.gammf.collabora.communication.actors

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import akka.pattern.ask
import akka.testkit.{ImplicitSender, TestKit}
import akka.util.Timeout
import org.gammf.collabora.{TestMessageUtil, TestUtil}
import org.gammf.collabora.communication.messages._

import org.gammf.collabora.util.UpdateMessage
import org.gammf.collabora.yellowpages.ActorContainer
import org.gammf.collabora.yellowpages.ActorService._
import org.gammf.collabora.yellowpages.messages._
import org.gammf.collabora.yellowpages.util.Topic
import org.gammf.collabora.yellowpages.TopicElement._
import org.scalatest.concurrent.Eventually
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}
import play.api.libs.json.Json

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.language.postfixOps
import org.scalatest.concurrent.Eventually._
import org.scalatest.time.{Millis, Seconds, Span}

class RabbitMQUpdatesReceiverActorTest extends TestKit (ActorSystem("CollaboraTest")) with WordSpecLike  with Matchers with BeforeAndAfterAll with ImplicitSender {

  implicit protected[this] val askTimeout: Timeout = Timeout(5 second)
  implicit val patienceConfig: Eventually.PatienceConfig =
    PatienceConfig(timeout = scaled(Span(2, Seconds)), interval = scaled(Span(5, Millis)))


  var rootYellowPages: ActorRef = _
  var receivedMessage: Option[UpdateMessage] = None

  override def beforeAll(): Unit = {
    ActorContainer.init()
    ActorContainer.createAll()
    rootYellowPages = ActorContainer.rootYellowPages

    removeDBMasterActorFromYellowPages()
    subscribeFakeDBMasterActor()

    Thread.sleep(200)
  }

  override def afterAll(): Unit = {
    ActorContainer.shutdown()
    TestKit.shutdownActorSystem(system)
  }


  "A UpdatesReceiver actor" should {

    "correctly send received messages to DBMaster actor" in {
      within(TestUtil.TASK_WAIT_TIME seconds){
        Await.result(rootYellowPages ? ActorRequestMessage(Topic() :+ Communication :+ Updates :+ RabbitMQ, Master), askTimeout.duration)
          .asInstanceOf[ActorResponseMessage] match {
          case response: ActorResponseOKMessage => response.actor ! ClientUpdateMessage(Json.toJson(TestMessageUtil.noteUpdateMessage).toString())
          case _ => fail
        }
        eventually {
          receivedMessage should not be None
        }
        assert(receivedMessage.get == TestMessageUtil.noteUpdateMessage)
      }
    }
  }

  private def removeDBMasterActorFromYellowPages(): Unit = {
    Thread.sleep(200)
    Await.result(rootYellowPages ? ActorRequestMessage(Topic() :+ Database, Master), askTimeout.duration)
      .asInstanceOf[ActorResponseMessage] match {
      case response: ActorResponseOKMessage => rootYellowPages ! DeletionRequestMessage(response.actor, "DBMaster", Topic() :+ Database, Master)
      case _ => fail
    }
  }

  private def subscribeFakeDBMasterActor(): Unit = {
    val newDBMaster = ActorContainer.actorSystem.actorOf(Props(new Actor {
      override def receive: Receive = {
        case message: UpdateMessage => receivedMessage = Some(message)
      }
    }))
    rootYellowPages ! RegistrationRequestMessage(newDBMaster, "FakeDBMaster", Topic() :+ Database, Master)
  }
}

