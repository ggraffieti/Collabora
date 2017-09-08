package org.gammf.collabora.database.actors
import akka.actor.{ActorRef, ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit}
import com.newmotion.akka.rabbitmq.{ConnectionActor, ConnectionFactory}
import org.gammf.collabora.TestUtil
import org.gammf.collabora.communication.actors._
import org.gammf.collabora.database.messages._
import org.gammf.collabora.util.{Module, Note, NoteState, SimpleModule, SimpleNote}
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

import scala.concurrent.duration._

class DBWorkerModulesActorTest extends TestKit (ActorSystem("CollaboraServer")) with WordSpecLike  with Matchers with BeforeAndAfterAll with ImplicitSender {

  val CONNECTION_ACTOR_NAME = "rabbitmq"
  val NAMING_ACTOR_NAME = "naming"
  val CHANNEL_CREATOR_NAME = "channelCreator"
  val PUBLISHER_ACTOR_NAME = "publisher"

  val factory = new ConnectionFactory()
  val connection:ActorRef = system.actorOf(ConnectionActor.props(factory), CONNECTION_ACTOR_NAME)
  val naming:ActorRef = system.actorOf(Props[RabbitMQNamingActor], NAMING_ACTOR_NAME)
  val channelCreator :ActorRef= system.actorOf(Props[ChannelCreatorActor], CHANNEL_CREATOR_NAME)
  val publisherActor:ActorRef = system.actorOf(Props[PublisherActor], PUBLISHER_ACTOR_NAME)

  val collaborationMemberActor:ActorRef = system.actorOf(Props(new CollaborationMembersActor(connection, naming, channelCreator, publisherActor)))
  val notificationActor:ActorRef = system.actorOf(Props(new NotificationsSenderActor(connection, naming, channelCreator, publisherActor,system)))
  val dbConnectionActor :ActorRef= system.actorOf(Props[ConnectionManagerActor])
  val dbMasterActor:ActorRef = system.actorOf(Props.create(classOf[DBMasterActor], system, notificationActor,collaborationMemberActor))
  val connectionManagerActor: ActorRef =  system.actorOf(Props[ConnectionManagerActor])
  val modulesActor:ActorRef = system.actorOf(Props.create(classOf[DBWorkerModulesActor], connectionManagerActor))

  val MODULE_ID:String = "123456788000000000000000"
  val MODULE_DESCRIPTION = "questo è un modulo importante"
  val MODULE_STATE = "doing"

  val module:Module = SimpleModule(Option(MODULE_ID),MODULE_DESCRIPTION,None,MODULE_STATE)

  override def beforeAll(): Unit = {

  }

  override def afterAll(): Unit = {
    TestKit.shutdownActorSystem(system)
  }

  "A DBWorkerModules actor" should {
    "insert new modules in a collaboration correctly in the db" in {
      within(TestUtil.TASK_WAIT_TIME second) {
        modulesActor ! InsertModuleMessage(module, TestUtil.FAKE_ID, TestUtil.FAKE_USER_ID)
        expectMsgType[QueryOkMessage]
      }
    }

    "update a module in a collaboration correctly" in {
      within(TestUtil.TASK_WAIT_TIME second) {
        modulesActor ! UpdateModuleMessage(module, TestUtil.FAKE_ID, TestUtil.FAKE_USER_ID)
        expectMsgType[QueryOkMessage]
      }
    }

    "delete a module in a collaboration correctly" in {
      within(TestUtil.TASK_WAIT_TIME second) {
        modulesActor ! DeleteModuleMessage(module, TestUtil.FAKE_ID, TestUtil.FAKE_USER_ID)
        expectMsgType[QueryOkMessage]
      }
    }
  }
}

