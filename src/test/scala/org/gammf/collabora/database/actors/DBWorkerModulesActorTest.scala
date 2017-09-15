package org.gammf.collabora.database.actors

import akka.actor.ActorSystem
import akka.testkit.{ImplicitSender, TestKit}
import akka.pattern.ask
import akka.util.Timeout
import com.newmotion.akka.rabbitmq.{ConnectionActor, ConnectionFactory}
import org.gammf.collabora.TestUtil
import org.gammf.collabora.database.actors.master.DBMasterActor
import org.gammf.collabora.database.actors.worker.DBWorkerModulesActor
import org.gammf.collabora.database.messages._
import org.gammf.collabora.util.SimpleModule
import org.gammf.collabora.yellowpages.ActorCreator
import org.gammf.collabora.yellowpages.ActorService.{ConnectionHandler, DefaultWorker}
import org.gammf.collabora.yellowpages.actors.YellowPagesActor
import org.gammf.collabora.yellowpages.messages._
import org.gammf.collabora.yellowpages.util.Topic
import org.gammf.collabora.yellowpages.TopicElement._
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

class DBWorkerModulesActorTest extends TestKit (ActorSystem("CollaboraServer")) with WordSpecLike  with Matchers with BeforeAndAfterAll with ImplicitSender {

  implicit protected[this] val askTimeout: Timeout = Timeout(5 second)
  val actorCreator = new ActorCreator(system)
  actorCreator.startCreation
  val rootYellowPages = actorCreator.getYellowPagesRoot

  val MODULE_ID:String = "123456788000000000000000"
  val MODULE_DESCRIPTION = "questo è un modulo importante"
  val MODULE_STATE = "doing"

  val module:org.gammf.collabora.util.Module = org.gammf.collabora.util.SimpleModule(Option(MODULE_ID),MODULE_DESCRIPTION,MODULE_STATE)

 // val module:Module = Module(Option(MODULE_ID),MODULE_DESCRIPTION,MODULE_STATE)


  override def beforeAll(): Unit = {

  }

  override def afterAll(): Unit = {
    TestKit.shutdownActorSystem(system)
  }

  "A DBWorkerModules actor" should {
    "insert new modules in a collaboration correctly in the db" in {
      within(TestUtil.TASK_WAIT_TIME second) {
        (rootYellowPages ? ActorRequestMessage(Topic() :+ Database :+ Module, DefaultWorker))
          .mapTo[ActorResponseMessage].map {
          case response: ActorResponseOKMessage => response.actor ! InsertModuleMessage(module, TestUtil.FAKE_ID, TestUtil.USER_ID)
          case _ =>

            expectMsg(RegistrationResponseMessage())
        }
      }
    }

    "update a module in a collaboration correctly" in {
      within(TestUtil.TASK_WAIT_TIME second) {
        (rootYellowPages ? ActorRequestMessage(Topic() :+ Database :+ Module, DefaultWorker))
          .mapTo[ActorResponseMessage].map {
          case response: ActorResponseOKMessage => response.actor ! UpdateModuleMessage(module, TestUtil.FAKE_ID, TestUtil.USER_ID)
          case _ =>

            expectMsgType[QueryOkMessage]
        }
      }
    }

    "delete a module in a collaboration correctly" in {
      within(TestUtil.TASK_WAIT_TIME second) {
        (rootYellowPages ? ActorRequestMessage(Topic() :+ Database :+ Module, DefaultWorker))
          .mapTo[ActorResponseMessage].map {
          case response: ActorResponseOKMessage => response.actor ! DeleteModuleMessage(module, TestUtil.FAKE_ID, TestUtil.USER_ID)
          case _ =>

            expectMsgType[QueryOkMessage]
        }
      }
    }
  }

}

