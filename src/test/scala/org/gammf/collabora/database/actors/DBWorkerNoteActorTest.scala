package org.gammf.collabora.database.actors

import akka.actor.{ActorRef, ActorSystem}
import akka.testkit.{ImplicitSender, TestKit}
import akka.pattern.ask
import akka.util.Timeout
import org.gammf.collabora.TestUtil
import org.gammf.collabora.database.messages._
import org.gammf.collabora.util.{Note, NoteState}
import org.gammf.collabora.yellowpages.{ActorContainer, TopicElement}
import org.gammf.collabora.yellowpages.ActorService.DefaultWorker
import org.gammf.collabora.yellowpages.messages._
import org.gammf.collabora.yellowpages.util.Topic
import org.gammf.collabora.yellowpages.TopicElement._
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.language.postfixOps

class DBWorkerNoteActorTest extends TestKit (ActorSystem("CollaboraTest")) with WordSpecLike  with Matchers with BeforeAndAfterAll with ImplicitSender {

  val NOTE_ID:String = "123456788354670000000000"
  val NOTE_CONTENT = "test"
  val NOTE_STATE_DONE = "done"

  val fakeNote = Note(Option(NOTE_ID), NOTE_CONTENT, None,
    None, None, new NoteState(NOTE_STATE_DONE,None), None)

  implicit protected[this] val askTimeout: Timeout = Timeout(5 second)
  var rootYellowPages: ActorRef = _

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

  "A DBWorkerNotes actor" should {
    "insert new notes correctly in the db" in {

      within(TestUtil.TASK_WAIT_TIME second) {
        Await.result(rootYellowPages ? ActorRequestMessage(Topic() :+ Database :+ TopicElement.Note, DefaultWorker), askTimeout.duration)
          .asInstanceOf[ActorResponseMessage] match {
          case response: ActorResponseOKMessage =>
            response.actor ! InsertNoteMessage(fakeNote, TestUtil.FAKE_ID, TestUtil.USER_ID)
            expectMsgPF() {
              case QueryOkMessage(query) => assert(query.isInstanceOf[InsertNoteMessage])
            }
          case _ => fail
        }
      }
    }

    "update notes correctly" in {
      within(TestUtil.TASK_WAIT_TIME second) {
        Await.result(rootYellowPages ? ActorRequestMessage(Topic() :+ Database :+ TopicElement.Note, DefaultWorker), askTimeout.duration)
          .asInstanceOf[ActorResponseMessage] match {
          case response: ActorResponseOKMessage =>
            response.actor ! UpdateNoteMessage(fakeNote, TestUtil.FAKE_ID, TestUtil.USER_ID)
            expectMsgPF() {
              case QueryOkMessage(query) => assert(query.isInstanceOf[UpdateNoteMessage])
            }
          case _ => fail
        }
      }
    }

    "delete notes correctly" in {
      within(TestUtil.TASK_WAIT_TIME second) {
        Await.result(rootYellowPages ? ActorRequestMessage(Topic() :+ Database :+ TopicElement.Note, DefaultWorker), askTimeout.duration)
          .asInstanceOf[ActorResponseMessage] match {
          case response: ActorResponseOKMessage =>
            response.actor ! DeleteNoteMessage(fakeNote, TestUtil.FAKE_ID, TestUtil.USER_ID)
            expectMsgPF() {
              case QueryOkMessage(query) => assert(query.isInstanceOf[DeleteNoteMessage])
            }
          case _ => fail
        }
      }
    }
  }

}

