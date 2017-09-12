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
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

import scala.concurrent.duration._

class DBWorkerMemberActorTest extends TestKit (ActorSystem("CollaboraServer")) with WordSpecLike  with Matchers with BeforeAndAfterAll with ImplicitSender {

  val CONNECTION_ACTOR_NAME = "rabbitmq"
  val NAMING_ACTOR_NAME = "naming"
  val CHANNEL_CREATOR_NAME = "channelCreator"
  val PUBLISHER_ACTOR_NAME = "publisher"

  val COLLABORATION_USERNAME = "peru"

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
  val usersActor:ActorRef = system.actorOf(Props.create(classOf[DBWorkerMemberActor], connectionManagerActor))

  val user = CollaborationUser(COLLABORATION_USERNAME, CollaborationRight.WRITE)

  override def beforeAll(): Unit = {

  }

  override def afterAll(): Unit = {
    TestKit.shutdownActorSystem(system)
  }

  "A DBWorkerMember actor" should {
    "insert new user in a collaboration correctly in the db" in {

      within(TestUtil.TASK_WAIT_TIME second) {
        usersActor ! InsertMemberMessage(user, TestUtil.FAKE_ID, TestUtil.USER_ID)
        expectMsgType[QueryOkMessage]
      }
    }

    "update a user right in a collaboration correctly" in {

      within(TestUtil.TASK_WAIT_TIME second) {
        usersActor ! UpdateMemberMessage(user, TestUtil.FAKE_ID, TestUtil.USER_ID)
        expectMsgType[QueryOkMessage]
      }

      within(TestUtil.TASK_WAIT_TIME second) {
        usersActor ! DeleteMemberMessage(user, TestUtil.FAKE_ID, TestUtil.USER_ID)
        expectMsgType[QueryOkMessage]
      }
    }
  }
}

