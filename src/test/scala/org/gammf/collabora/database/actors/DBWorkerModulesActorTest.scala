package org.gammf.collabora.database.actors
import akka.actor.ActorSystem
import akka.testkit.{ImplicitSender, TestKit}
import com.newmotion.akka.rabbitmq.{ConnectionActor, ConnectionFactory}
import org.gammf.collabora.TestUtil
import org.gammf.collabora.database.actors.master.DBMasterActor
import org.gammf.collabora.database.actors.worker.DBWorkerModulesActor
import org.gammf.collabora.database.messages._
import org.gammf.collabora.util.{SimpleModule}
import org.gammf.collabora.yellowpages.ActorService.ConnectionHandler
import org.gammf.collabora.yellowpages.actors.YellowPagesActor
import org.gammf.collabora.yellowpages.messages.{RegistrationRequestMessage, RegistrationResponseMessage}
import org.gammf.collabora.yellowpages.util.Topic
import org.gammf.collabora.yellowpages.TopicElement._
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

import scala.concurrent.duration._

class DBWorkerModulesActorTest extends TestKit (ActorSystem("CollaboraServer")) with WordSpecLike  with Matchers with BeforeAndAfterAll with ImplicitSender {

  val CONNECTION_ACTOR_NAME = "RabbitConnection"
  val MONGO_CONNECTION_ACTOR_NAME = "MongoConnectionManager"
  val DBMASTER_ACTOR_NAME = "DBMaster"
  val DBWORKER_MODULES_ACTOR_NAME = "DBWorkerModules"

  val rootYellowPages = system.actorOf(YellowPagesActor.rootProps())
  val factory = new ConnectionFactory()
  val rabbitConnection = system.actorOf(ConnectionActor.props(factory), CONNECTION_ACTOR_NAME)
  rootYellowPages ! RegistrationRequestMessage(rabbitConnection, CONNECTION_ACTOR_NAME, Topic() :+ Communication :+ RabbitMQ, ConnectionHandler)

  val mongoConnectionActor = system.actorOf(ConnectionManagerActor.printerProps(rootYellowPages, Topic() :+ Database, MONGO_CONNECTION_ACTOR_NAME))
  val dbMasterActor = system.actorOf(DBMasterActor.printerProps(rootYellowPages, Topic() :+ Database, DBMASTER_ACTOR_NAME))
  val dBWorkerModulesActor = system.actorOf(DBWorkerModulesActor.printerProps(rootYellowPages, Topic() :+ Database :+ Module, DBWORKER_MODULES_ACTOR_NAME))


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
        dBWorkerModulesActor ! InsertModuleMessage(module, TestUtil.FAKE_ID, TestUtil.USER_ID)
        expectMsg(RegistrationResponseMessage())
      }
    }

    "update a module in a collaboration correctly" in {
      within(TestUtil.TASK_WAIT_TIME second) {
        dBWorkerModulesActor ! UpdateModuleMessage(module, TestUtil.FAKE_ID, TestUtil.USER_ID)
        expectMsgType[QueryOkMessage]
      }
    }

    "delete a module in a collaboration correctly" in {
      within(TestUtil.TASK_WAIT_TIME second) {
        dBWorkerModulesActor ! DeleteModuleMessage(module, TestUtil.FAKE_ID, TestUtil.USER_ID)
        expectMsgType[QueryOkMessage]
      }
    }
  }

}

