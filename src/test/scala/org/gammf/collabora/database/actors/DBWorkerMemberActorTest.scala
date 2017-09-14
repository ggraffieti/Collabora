package org.gammf.collabora.database.actors

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit}
import com.newmotion.akka.rabbitmq.{ConnectionActor, ConnectionFactory}
import org.gammf.collabora.TestUtil
import org.gammf.collabora.communication.actors._
import org.gammf.collabora.database.actors.master.DBMasterActor
import org.gammf.collabora.database.actors.worker.DBWorkerMemberActor
import org.gammf.collabora.database.messages._
import org.gammf.collabora.util.{CollaborationRight, CollaborationUser, SimpleUser}
import org.gammf.collabora.yellowpages.ActorService.ConnectionHandler
import org.gammf.collabora.yellowpages.actors.YellowPagesActor
import org.gammf.collabora.yellowpages.messages.{RegistrationRequestMessage, RegistrationResponseMessage}
import org.gammf.collabora.yellowpages.util.Topic
import org.gammf.collabora.yellowpages.TopicElement._
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

import scala.concurrent.duration._

class DBWorkerMemberActorTest extends TestKit (ActorSystem("CollaboraServer")) with WordSpecLike  with Matchers with BeforeAndAfterAll with ImplicitSender {

  val COLLABORATION_USERNAME = "peru"

  val CONNECTION_ACTOR_NAME = "RabbitConnection"
  val MONGO_CONNECTION_ACTOR_NAME = "MongoConnectionManager"
  val DBMASTER_ACTOR_NAME = "DBMaster"
  val DBWORKER_MEMBER_ACTOR_NAME = "DBWorkerMembers"

  val rootYellowPages = system.actorOf(YellowPagesActor.rootProps())
  val factory = new ConnectionFactory()
  val rabbitConnection = system.actorOf(ConnectionActor.props(factory), CONNECTION_ACTOR_NAME)
  rootYellowPages ! RegistrationRequestMessage(rabbitConnection, CONNECTION_ACTOR_NAME, Topic() :+ Communication :+ RabbitMQ, ConnectionHandler)

  val mongoConnectionActor = system.actorOf(ConnectionManagerActor.printerProps(rootYellowPages, Topic() :+ Database, MONGO_CONNECTION_ACTOR_NAME))
  val dbMasterActor = system.actorOf(DBMasterActor.printerProps(rootYellowPages, Topic() :+ Database, DBMASTER_ACTOR_NAME))
  val dbWorkerMembersActor = system.actorOf(DBWorkerMemberActor.printerProps(rootYellowPages, Topic() :+ Database :+ Member, DBWORKER_MEMBER_ACTOR_NAME))

  val user = CollaborationUser(COLLABORATION_USERNAME, CollaborationRight.WRITE)

  override def beforeAll(): Unit = {

  }

  override def afterAll(): Unit = {
    TestKit.shutdownActorSystem(system)
  }

  "A DBWorkerMember actor" should {
    "insert new user in a collaboration correctly in the db" in {

      within(TestUtil.TASK_WAIT_TIME second) {
        dbWorkerMembersActor ! InsertMemberMessage(user, TestUtil.FAKE_ID, TestUtil.USER_ID)
        expectMsg(RegistrationResponseMessage())
      }
    }

    "update a user right in a collaboration correctly" in {

      within(TestUtil.TASK_WAIT_TIME second) {
        dbWorkerMembersActor ! UpdateMemberMessage(user, TestUtil.FAKE_ID, TestUtil.USER_ID)
        expectMsgType[QueryOkMessage]
      }

      within(TestUtil.TASK_WAIT_TIME second) {
        dbWorkerMembersActor ! DeleteMemberMessage(user, TestUtil.FAKE_ID, TestUtil.USER_ID)
        expectMsgType[QueryOkMessage]
      }
    }
  }

}

