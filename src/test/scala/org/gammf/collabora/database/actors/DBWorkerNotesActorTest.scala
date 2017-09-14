package org.gammf.collabora.database.actors

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit}
import com.newmotion.akka.rabbitmq.{ConnectionActor, ConnectionFactory}
import org.gammf.collabora.TestUtil
import org.gammf.collabora.communication.actors._
import org.gammf.collabora.database.actors.master.DBMasterActor
import org.gammf.collabora.database.actors.worker.DBWorkerNotesActor
import org.gammf.collabora.database.messages._
import org.gammf.collabora.util.{NoteState, SimpleNote}
import org.gammf.collabora.yellowpages.ActorService.ConnectionHandler
import org.gammf.collabora.yellowpages.actors.YellowPagesActor
import org.gammf.collabora.yellowpages.messages.{RegistrationRequestMessage, RegistrationResponseMessage}
import org.gammf.collabora.yellowpages.util.Topic
import org.gammf.collabora.yellowpages.TopicElement._
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

import scala.concurrent.duration._

class DBWorkerNotesActorTest extends TestKit (ActorSystem("CollaboraServer")) with WordSpecLike  with Matchers with BeforeAndAfterAll with ImplicitSender {

  val NOTE_ID:String = "123456788354670000000000"
  val NOTE_CONTENT = "prova test"
  val NOTE_STATE_DONE = "done"

  val notetmp:org.gammf.collabora.util.Note = org.gammf.collabora.util.SimpleNote(Option(NOTE_ID), NOTE_CONTENT, None,
    None, None, new NoteState(NOTE_STATE_DONE,None), None)

  val CONNECTION_ACTOR_NAME = "RabbitConnection"
  val MONGO_CONNECTION_ACTOR_NAME = "MongoConnectionManager"
  val DBMASTER_ACTOR_NAME = "DBMaster"
  val DBWORKER_NOTES_ACTOR_NAME = "DBWorkerNotes"

  val rootYellowPages = system.actorOf(YellowPagesActor.rootProps())
  val factory = new ConnectionFactory()
  val rabbitConnection = system.actorOf(ConnectionActor.props(factory), CONNECTION_ACTOR_NAME)
  rootYellowPages ! RegistrationRequestMessage(rabbitConnection, CONNECTION_ACTOR_NAME, Topic() :+ Communication :+ RabbitMQ, ConnectionHandler)

  val mongoConnectionActor = system.actorOf(ConnectionManagerActor.printerProps(rootYellowPages, Topic() :+ Database, MONGO_CONNECTION_ACTOR_NAME))
  val dbMasterActor = system.actorOf(DBMasterActor.printerProps(rootYellowPages, Topic() :+ Database, DBMASTER_ACTOR_NAME))
  val dBWorkerNotesActor = system.actorOf(DBWorkerNotesActor.printerProps(rootYellowPages, Topic() :+ Database :+ Note, DBWORKER_NOTES_ACTOR_NAME))

  override def beforeAll(): Unit = {

  }

  override def afterAll(): Unit = {
    TestKit.shutdownActorSystem(system)
  }

  "A DBWorkerNotes actor" should {
    "insert new notes correctly in the db" in {

      within(TestUtil.TASK_WAIT_TIME second) {
        dBWorkerNotesActor ! InsertNoteMessage(notetmp, TestUtil.FAKE_ID, TestUtil.USER_ID)
        expectMsg(RegistrationResponseMessage())
      }
    }

    "update notes correctly" in {
      within(TestUtil.TASK_WAIT_TIME second) {
        dBWorkerNotesActor ! UpdateNoteMessage(notetmp, TestUtil.FAKE_ID, TestUtil.USER_ID)
        expectMsgType[QueryOkMessage]
      }
    }

    "delete notes correctly" in {
      within(TestUtil.TASK_WAIT_TIME second) {
        dBWorkerNotesActor ! DeleteNoteMessage(notetmp, TestUtil.FAKE_ID, TestUtil.USER_ID)
        expectMsgType[QueryOkMessage]
      }
    }
  }

}

