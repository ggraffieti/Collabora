package org.gammf.collabora.database.actors

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit}
import akka.pattern.ask
import akka.util.Timeout
import com.newmotion.akka.rabbitmq.{ConnectionActor, ConnectionFactory}
import org.gammf.collabora.TestUtil
import org.gammf.collabora.communication.actors._
import org.gammf.collabora.database.actors.master.DBMasterActor
import org.gammf.collabora.database.actors.worker.DBWorkerNoteActor
import org.gammf.collabora.database.messages._
import org.gammf.collabora.util.{NoteState, SimpleNote}
import org.gammf.collabora.yellowpages.ActorCreator
import org.gammf.collabora.yellowpages.ActorService.{ConnectionHandler, DefaultWorker}
import org.gammf.collabora.yellowpages.actors.YellowPagesActor
import org.gammf.collabora.yellowpages.messages._
import org.gammf.collabora.yellowpages.util.Topic
import org.gammf.collabora.yellowpages.TopicElement._
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

class DBWorkerNoteActorTest extends TestKit (ActorSystem("CollaboraServer")) with WordSpecLike  with Matchers with BeforeAndAfterAll with ImplicitSender {

  val NOTE_ID:String = "123456788354670000000000"
  val NOTE_CONTENT = "prova test"
  val NOTE_STATE_DONE = "done"

  val notetmp:org.gammf.collabora.util.Note = org.gammf.collabora.util.SimpleNote(Option(NOTE_ID), NOTE_CONTENT, None,
    None, None, new NoteState(NOTE_STATE_DONE,None), None)

  implicit protected[this] val askTimeout: Timeout = Timeout(5 second)
  val actorCreator = new ActorCreator(system)
  actorCreator.startCreation
  val rootYellowPages = actorCreator.getYellowPagesRoot

  override def beforeAll(): Unit = {

  }

  override def afterAll(): Unit = {
    TestKit.shutdownActorSystem(system)
  }

  "A DBWorkerNotes actor" should {
    "insert new notes correctly in the db" in {

      within(TestUtil.TASK_WAIT_TIME second) {
        (rootYellowPages ? ActorRequestMessage(Topic() :+ Database :+ Note, DefaultWorker))
          .mapTo[ActorResponseMessage].map {
          case response: ActorResponseOKMessage => response.actor ! InsertNoteMessage(notetmp, TestUtil.FAKE_ID, TestUtil.USER_ID)
          case _ =>

            expectMsg(RegistrationResponseMessage())
        }
      }
    }

    "update notes correctly" in {
      within(TestUtil.TASK_WAIT_TIME second) {
        (rootYellowPages ? ActorRequestMessage(Topic() :+ Database :+ Note, DefaultWorker))
          .mapTo[ActorResponseMessage].map {
          case response: ActorResponseOKMessage => response.actor ! UpdateNoteMessage(notetmp, TestUtil.FAKE_ID, TestUtil.USER_ID)
          case _ =>

            expectMsgType[QueryOkMessage]
        }
      }
    }

    "delete notes correctly" in {
      within(TestUtil.TASK_WAIT_TIME second) {
        (rootYellowPages ? ActorRequestMessage(Topic() :+ Database :+ Note, DefaultWorker))
          .mapTo[ActorResponseMessage].map {
          case response: ActorResponseOKMessage => response.actor ! DeleteNoteMessage(notetmp, TestUtil.FAKE_ID, TestUtil.USER_ID)
          case _ =>

            expectMsgType[QueryOkMessage]
        }
      }
    }
  }

}

