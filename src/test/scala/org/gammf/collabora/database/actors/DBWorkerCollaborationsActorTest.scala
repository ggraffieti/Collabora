package org.gammf.collabora.database.actors

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit}
import com.newmotion.akka.rabbitmq.{ConnectionActor, ConnectionFactory}
import org.gammf.collabora.TestUtil
import org.gammf.collabora.communication.actors._
import org.gammf.collabora.database.actors.master.DBMasterActor
import org.gammf.collabora.database.actors.worker.DBWorkerCollaborationsActor
import org.gammf.collabora.database.messages._
import org.gammf.collabora.util.{Collaboration, CollaborationRight, CollaborationType, CollaborationUser, Location, Module, NoteState, SimpleCollaboration, SimpleModule, SimpleNote}
import org.joda.time.DateTime
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

import scala.concurrent.duration._

class DBWorkerCollaborationsActorTest extends TestKit (ActorSystem("CollaboraServer")) with WordSpecLike  with Matchers with BeforeAndAfterAll with ImplicitSender {

/*
  val CONNECTION_ACTOR_NAME = "rabbitmq"
  val NAMING_ACTOR_NAME = "naming"
  val CHANNEL_CREATOR_NAME = "channelCreator"
  val PUBLISHER_ACTOR_NAME = "publisher"

  val COLLABORATION_ID:String = "123456788698540008123400"
  val COLLABORATION_NAME = "simplecollaboration"
  val COLLABORATION_USER_FONE = "fone"
  val COLLABORATION_USER_PERU = "peru"
  val COLLABORATION_FIRST_NOTE_CONTENT = "questo è il contenuto"
  val COLLABORATION_FIRST_LOCATION_LATITUDE = 23.32
  val COLLABORATION_FIRST_LOCATION_LONGITUDE = 23.42
  val COLLABORATION_SECOND_LOCATION_LATITUDE = 233.32
  val COLLABORATION_SECOND_LOCATION_LONGITUDE = 233.42
  val COLLABORATION_SECOND_NOTE_CONTENT = "questo è il contenuto2"
  val COLLABORATION_STATE_DOING = "doing"
  val COLLABORATION_STATE_DONE = "done"

  val factory = new ConnectionFactory()
  val connection:ActorRef = system.actorOf(ConnectionActor.props(factory), CONNECTION_ACTOR_NAME)
  val naming:ActorRef = system.actorOf(Props[RabbitMQNamingActor], NAMING_ACTOR_NAME)
  val channelCreator :ActorRef= system.actorOf(Props[ChannelCreatorActor], CHANNEL_CREATOR_NAME)
  val publisherActor:ActorRef = system.actorOf(Props[PublisherActor], PUBLISHER_ACTOR_NAME)

  val factory = new ConnectionFactory()
  val connection:ActorRef = system.actorOf(ConnectionActor.props(factory), "rabbitmq")
  val naming:ActorRef = system.actorOf(Props[RabbitMQNamingActor], "naming")
  val channelCreator :ActorRef= system.actorOf(Props[ChannelCreatorActor], "channelCreator")
  val publisherActor:ActorRef = system.actorOf(Props[PublisherActor], "publisher")
>>>>>>> e1352d43aebaf97ca96e951fc473704c444d2b97
  val collaborationMemberActor:ActorRef = system.actorOf(Props(new CollaborationMembersActor(connection, naming, channelCreator, publisherActor)))
  val notificationActor:ActorRef = system.actorOf(Props(new NotificationsSenderActor(connection, naming, channelCreator, publisherActor,system)))
  val dbConnectionActor :ActorRef= system.actorOf(Props[ConnectionManagerActor])
  val dbMasterActor:ActorRef = system.actorOf(Props.create(classOf[DBMasterActor], system, notificationActor,collaborationMemberActor))
  val connectionManagerActor: ActorRef =  system.actorOf(Props[ConnectionManagerActor])
  val collaborationsActor:ActorRef = system.actorOf(Props.create(classOf[DBWorkerCollaborationsActor], connectionManagerActor))

  val collab:Collaboration = SimpleCollaboration(
    id = Some(COLLABORATION_ID),
    name = COLLABORATION_NAME,
    collaborationType = CollaborationType.GROUP,
    users = Some(List(CollaborationUser(COLLABORATION_USER_FONE, CollaborationRight.ADMIN), CollaborationUser(COLLABORATION_USER_PERU, CollaborationRight.ADMIN))),
    modules = Option.empty,
    notes = Some(List(SimpleNote(None, COLLABORATION_FIRST_NOTE_CONTENT,Some(new DateTime()),Some(Location(COLLABORATION_FIRST_LOCATION_LATITUDE,COLLABORATION_FIRST_LOCATION_LONGITUDE)),Option.empty,NoteState(COLLABORATION_STATE_DOING, Some(COLLABORATION_USER_PERU)),None),
      SimpleNote(None,COLLABORATION_SECOND_NOTE_CONTENT,Some(new DateTime()),Some(Location(COLLABORATION_SECOND_LOCATION_LATITUDE,COLLABORATION_SECOND_LOCATION_LONGITUDE)),None,NoteState(COLLABORATION_STATE_DONE, Option(COLLABORATION_USER_PERU)),None)))
  )

  override def beforeAll(): Unit = {

  }

  override def afterAll(): Unit = {
    TestKit.shutdownActorSystem(system)
  }

  "A DBWorkerCollaborations actor" should {
    "insert new collaboration in the db" in {

      within(TestUtil.TASK_WAIT_TIME second) {
        collaborationsActor ! InsertCollaborationMessage(collab, TestUtil.USER_ID)
        expectMsgType[QueryOkMessage]
      }
    }

    "update a collaboration in the db" in {
      within(TestUtil.TASK_WAIT_TIME second) {
        collaborationsActor ! UpdateCollaborationMessage(collab, TestUtil.USER_ID)
        expectMsgType[QueryOkMessage]
      }
    }

    "delete a collaboration in the db" in {
      within(TestUtil.TASK_WAIT_TIME second) {
        collaborationsActor ! DeleteCollaborationMessage(collab, TestUtil.USER_ID)
        expectMsgType[QueryOkMessage]
      }
    }
  }*/
}
