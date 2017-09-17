package org.gammf.collabora.database.actors

import akka.actor.{ActorRef, ActorSystem}
import akka.testkit.{ImplicitSender, TestKit}
import akka.pattern.ask
import akka.util.Timeout
import org.gammf.collabora.TestUtil
import org.gammf.collabora.database.messages._
import org.gammf.collabora.util.Module
import org.gammf.collabora.yellowpages.{ActorContainer, TopicElement}
import org.gammf.collabora.yellowpages.ActorService.DefaultWorker
import org.gammf.collabora.yellowpages.messages._
import org.gammf.collabora.yellowpages.util.Topic
import org.gammf.collabora.yellowpages.TopicElement._
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.language.postfixOps

class DBWorkerModuleActorTest extends TestKit (ActorSystem("CollaboraTest")) with WordSpecLike  with Matchers with BeforeAndAfterAll with ImplicitSender {

  implicit protected[this] val askTimeout: Timeout = Timeout(5 second)

  var rootYellowPages: ActorRef = _

  val MODULE_ID:String = "123456788000000000000000"
  val MODULE_DESCRIPTION = "questo è un modulo importante"
  val MODULE_STATE = "doing"

  val module = Module(Option(MODULE_ID),MODULE_DESCRIPTION,MODULE_STATE)

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

  "A DBWorkerModules actor" should {
    "insert new modules in a collaboration correctly in the db" in {
      within(TestUtil.TASK_WAIT_TIME second) {
        Await.result(rootYellowPages ? ActorRequestMessage(Topic() :+ Database :+ TopicElement.Module, DefaultWorker), askTimeout.duration)
          .asInstanceOf[ActorResponseMessage] match {
          case response: ActorResponseOKMessage =>
            response.actor ! InsertModuleMessage(module, TestUtil.FAKE_ID, TestUtil.USER_ID)
            expectMsgPF() {
              case QueryOkMessage(query) => assert(query.isInstanceOf[InsertModuleMessage])
            }
          case _ => fail
        }
      }
    }

    "update a module in a collaboration correctly" in {
      within(TestUtil.TASK_WAIT_TIME second) {
        Await.result(rootYellowPages ? ActorRequestMessage(Topic() :+ Database :+ TopicElement.Module, DefaultWorker), askTimeout.duration)
          .asInstanceOf[ActorResponseMessage] match {
          case response: ActorResponseOKMessage =>
            response.actor ! UpdateModuleMessage(module, TestUtil.FAKE_ID, TestUtil.USER_ID)
            expectMsgPF() {
              case QueryOkMessage(query) => assert(query.isInstanceOf[UpdateModuleMessage])
            }
          case _ => fail
        }
      }
    }

    "delete a module in a collaboration correctly" in {
      within(TestUtil.TASK_WAIT_TIME second) {
        Await.result(rootYellowPages ? ActorRequestMessage(Topic() :+ Database :+ TopicElement.Module, DefaultWorker), askTimeout.duration)
          .asInstanceOf[ActorResponseMessage] match {
          case response: ActorResponseOKMessage =>
            response.actor ! DeleteModuleMessage(module, TestUtil.FAKE_ID, TestUtil.USER_ID)
            expectMsgPF() {
              case QueryOkMessage(query) => assert(query.isInstanceOf[DeleteModuleMessage])
            }
          case _ => fail
        }
      }
    }
  }
}

