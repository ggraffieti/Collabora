package org.gammf.collabora.database.actors

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit}
import com.newmotion.akka.rabbitmq.{ConnectionActor, ConnectionFactory}
import org.gammf.collabora.communication.actors._
import org.gammf.collabora.database.messages._
import org.gammf.collabora.util.{CollaborationRight, CollaborationUser}
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

import scala.concurrent.duration._

class DBWorkerMemberActorTest extends TestKit (ActorSystem("CollaboraServer")) with WordSpecLike  with Matchers with BeforeAndAfterAll with ImplicitSender {

  val CONNECTION_ACTOR_NAME = "rabbitmq"
  val NAMING_ACTOR_NAME = "naming"
  val CHANNEL_CREATOR_NAME = "channelCreator"
  val PUBLISHER_ACTOR_NAME = "publisher"
  val COLLABORATION_USERNAME = "peru"
  val TEST_COLLABORATION_ID = "59806a4af27da3fcfe0ac0ca"
  val TEST_USER_ID = "maffone"
  val TASK_WAIT_TIME = 5

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
      within(TASK_WAIT_TIME second) {
        usersActor ! InsertUserMessage(user, TEST_COLLABORATION_ID, TEST_USER_ID)
        expectMsgType[QueryOkMessage]
      }
    }

    "update a user right in a collaboration correctly" in {
      within(TASK_WAIT_TIME second) {
        usersActor ! UpdateUserMessage(user, TEST_COLLABORATION_ID, TEST_USER_ID)
        expectMsgType[QueryOkMessage]
      }
    }

    "delete a user in a collaboration correctly" in {
      within(TASK_WAIT_TIME second) {
        usersActor ! DeleteUserMessage(user, TEST_COLLABORATION_ID, TEST_USER_ID)
        expectMsgType[QueryOkMessage]
      }
    }



  }
}

