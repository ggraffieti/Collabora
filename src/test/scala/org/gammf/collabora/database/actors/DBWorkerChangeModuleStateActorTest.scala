package org.gammf.collabora.database.actors

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit}
import akka.util.Timeout
import akka.pattern.ask
import org.gammf.collabora.{TestMessageUtil, TestUtil}
import org.gammf.collabora.database.messages.ChangeModuleState
import org.gammf.collabora.util.UpdateMessage
import org.gammf.collabora.yellowpages.ActorContainer
import org.gammf.collabora.yellowpages.ActorService._
import org.gammf.collabora.yellowpages.messages._
import org.gammf.collabora.yellowpages.util.Topic
import org.gammf.collabora.yellowpages.TopicElement._
import org.scalatest.concurrent.Eventually
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

import scala.concurrent.duration._
import scala.concurrent.Await
import scala.language.postfixOps


class DBWorkerChangeModuleStateActorTest extends TestKit (ActorSystem("CollaboraTest")) with WordSpecLike with Eventually with Matchers with BeforeAndAfterAll with ImplicitSender {

  implicit protected[this] val askTimeout: Timeout = Timeout(5 second)
  override implicit val patienceConfig: PatienceConfig = PatienceConfig(
    timeout = scaled(TestUtil.TIMEOUT_SECOND seconds),
    interval = scaled(TestUtil.INTERVAL_MILLIS millis)
  )

  var rootYellowPages: ActorRef = _
  var expectedUpdateMessage: Option[UpdateMessage] = None

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

  "A DBWorkerChangeModuleState actor" should {
    "change the state of a module" in {
      Await.result(rootYellowPages ? ActorRequestMessage(Topic() :+ Database :+ Module, StateChanger), askTimeout.duration)
        .asInstanceOf[ActorResponseMessage] match {
        case response: ActorResponseOKMessage =>
          response.actor ! ChangeModuleState(TestMessageUtil.moduleStateChangeCollaborationID, TestMessageUtil.moduleStateChangeModuleID)
        case _ => fail
      }
      eventually {
        expectedUpdateMessage should not be None
      }
      assert(expectedUpdateMessage.get.module.isDefined &&
             expectedUpdateMessage.get.collaborationId.get == TestMessageUtil.moduleStateChangeCollaborationID &&
             expectedUpdateMessage.get.module.get.id.get == TestMessageUtil.moduleStateChangeModuleID)
    }

    "leave the state of a module unchanged" in {
      expectedUpdateMessage = None
      Await.result(rootYellowPages ? ActorRequestMessage(Topic() :+ Database :+ Module, StateChanger), askTimeout.duration)
        .asInstanceOf[ActorResponseMessage] match {
        case response: ActorResponseOKMessage =>
          response.actor ! ChangeModuleState(TestMessageUtil.moduleStateChangeCollaborationID, TestMessageUtil.moduleStateChangeModuleID)
        case _ => fail
      }
      eventually {
        assert(expectedUpdateMessage.isEmpty)
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
        case message: UpdateMessage => expectedUpdateMessage = Some(message)
      }
    }))
    rootYellowPages ! RegistrationRequestMessage(newDBMaster, "FakeDBMaster", Topic() :+ Database, Master)
  }

}
