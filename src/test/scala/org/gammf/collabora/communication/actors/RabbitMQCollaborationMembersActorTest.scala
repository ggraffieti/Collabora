package org.gammf.collabora.communication.actors

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import akka.pattern.ask
import akka.testkit.{DefaultTimeout, ImplicitSender, TestKit}
import akka.util.Timeout

import org.gammf.collabora.communication.messages._
import org.gammf.collabora.{TestMessageUtil, TestUtil}
import org.gammf.collabora.yellowpages.ActorContainer
import org.gammf.collabora.yellowpages.ActorService._
import org.gammf.collabora.yellowpages.messages._
import org.gammf.collabora.yellowpages.util.Topic
import org.gammf.collabora.yellowpages.TopicElement._

import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

import org.scalatest.concurrent.Eventually
import play.api.libs.json.Json

import scala.concurrent.duration._
import scala.concurrent.Await
import scala.language.postfixOps

class RabbitMQCollaborationMembersActorTest extends TestKit (ActorSystem("CollaboraTest")) with WordSpecLike with Eventually with DefaultTimeout with Matchers with BeforeAndAfterAll with ImplicitSender {

  implicit protected[this] val askTimeout: Timeout = Timeout(5 second)
  override implicit val patienceConfig: PatienceConfig = PatienceConfig(
    timeout = scaled(TestUtil.TIMEOUT_SECOND seconds),
    interval = scaled(TestUtil.INTERVAL_MILLIS millis)
  )

  var rootYellowPages: ActorRef = _
  var stringCollaborationMessage = ""

  override def beforeAll(): Unit = {
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

  "A CollaborationMember actor" should {

    "correctly send a publish message to the publisher actor when needed" in {
      Await.result(rootYellowPages ? ActorRequestMessage(Topic() :+ Communication :+ Collaborations :+ RabbitMQ, Master), askTimeout.duration)
        .asInstanceOf[ActorResponseMessage] match {
        case response: ActorResponseOKMessage => response.actor ! TestMessageUtil.publishCollaborationMessage
        case _ => fail
      }
      eventually {
        stringCollaborationMessage should not be ""
      }
      assert(stringCollaborationMessage == Json.toJson(TestMessageUtil.collaborationMessage).toString())
      stringCollaborationMessage = ""
    }

    "correcly send an error message to the publisher actor when needed" in {
      Await.result(rootYellowPages ? ActorRequestMessage(Topic() :+ Communication :+ Collaborations :+ RabbitMQ, Master), askTimeout.duration)
        .asInstanceOf[ActorResponseMessage] match {
        case response: ActorResponseOKMessage => response.actor ! TestMessageUtil.publishErrorCollaborationMessage
        case _ => fail
      }
      eventually {
        stringCollaborationMessage should not be ""
      }
      assert(stringCollaborationMessage == Json.toJson(TestMessageUtil.serverErrorMessage).toString())
    }

  }

  private def removePublisherActorFromYellowPages() : Unit = {
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
        case publishMessage: PublishMessage => stringCollaborationMessage = publishMessage.message.toString()
      }
    }))
    rootYellowPages ! RegistrationRequestMessage(fakePublisherActor, "fakePublisherActor", Topic() :+ Communication :+ RabbitMQ, Publishing)
  }
}
