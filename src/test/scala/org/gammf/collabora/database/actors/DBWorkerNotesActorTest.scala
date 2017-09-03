package org.gammf.collabora.database.actors

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit}
import com.newmotion.akka.rabbitmq.{ConnectionActor, ConnectionFactory}
import org.gammf.collabora.communication.actors._
import org.gammf.collabora.database.messages._
import org.gammf.collabora.util.{Note, NoteState, SimpleNote}
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}
import scala.concurrent.duration._

class DBWorkerNotesActorTest extends TestKit (ActorSystem("CollaboraServer")) with WordSpecLike  with Matchers with BeforeAndAfterAll with ImplicitSender {

  val CONNECTION_ACTOR_NAME = "rabbitmq"
  val NAMING_ACTOR_NAME = "naming"
  val CHANNEL_CREATOR_NAME = "channelCreator"
  val PUBLISHER_ACTOR_NAME = "publisher"

  val TEST_NOTE_ID = "59806a4af27da3fcfe0ac0ca"
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
  val notesActor:ActorRef = system.actorOf(Props.create(classOf[DBWorkerNotesActor], connectionManagerActor))

  val NOTE_ID:String = "123456788354670000000000"
  val NOTE_CONTENT = "prova test"
  val NOTE_STATE_DONE = "done"

  val notetmp:Note = SimpleNote(Option(NOTE_ID), NOTE_CONTENT, None,
    None, None, new NoteState(NOTE_STATE_DONE,None), None)

  override def beforeAll(): Unit = {

  }

  override def afterAll(): Unit = {
    TestKit.shutdownActorSystem(system)
  }

  "A DBWorkerNotes actor" should {
    "insert new notes correctly in the db" in {

      within(TASK_WAIT_TIME second) {
        notesActor ! InsertNoteMessage(notetmp, TEST_NOTE_ID, TEST_USER_ID)
        expectMsgType[QueryOkMessage]
      }
    }

    "update notes correctly" in {
      within(TASK_WAIT_TIME second) {
        notesActor ! UpdateNoteMessage(notetmp, TEST_NOTE_ID, TEST_USER_ID)
        expectMsgType[QueryOkMessage]
      }
    }

    "delete notes correctly" in {
      within(TASK_WAIT_TIME second) {
        notesActor ! DeleteNoteMessage(notetmp, TEST_NOTE_ID, TEST_USER_ID)
        expectMsgType[QueryOkMessage]
      }
    }



  }
}

