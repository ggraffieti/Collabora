package org.gammf.collabora.database.actors

import akka.actor.{ActorRef, ActorSystem}
import akka.testkit.{ImplicitSender, TestKit}
import akka.pattern.ask
import akka.util.Timeout
import org.gammf.collabora.TestUtil
import org.gammf.collabora.database.messages._
import org.gammf.collabora.util.{CollaborationRight, CollaborationType, CollaborationUser, Location, NoteState, SimpleCollaboration, SimpleNote}
import org.gammf.collabora.yellowpages.ActorContainer
import org.gammf.collabora.yellowpages.ActorService.DefaultWorker
import org.gammf.collabora.yellowpages.messages._
import org.gammf.collabora.yellowpages.util.Topic
import org.gammf.collabora.yellowpages.TopicElement._
import org.joda.time.DateTime
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.language.postfixOps

class DBWorkerCollaborationActorTest extends TestKit (ActorSystem("CollaboraTest")) with WordSpecLike  with Matchers with BeforeAndAfterAll with ImplicitSender {

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

  implicit protected[this] val askTimeout: Timeout = Timeout(5 second)

  var rootYellowPages: ActorRef = _

  override def beforeAll(): Unit = {
    ActorContainer.init()
    ActorContainer.createAll()
    rootYellowPages = ActorContainer.rootYellowPages
    Thread.sleep(200)
  }

  val collaboration = SimpleCollaboration(
    id = Some(COLLABORATION_ID),
    name = COLLABORATION_NAME,
    collaborationType = CollaborationType.GROUP,
    users = Some(List(CollaborationUser(COLLABORATION_USER_FONE, CollaborationRight.ADMIN), CollaborationUser(COLLABORATION_USER_PERU, CollaborationRight.ADMIN))),
    modules = Option.empty,
    notes = Some(List(SimpleNote(None, COLLABORATION_FIRST_NOTE_CONTENT,Some(new DateTime()),Some(Location(COLLABORATION_FIRST_LOCATION_LATITUDE,COLLABORATION_FIRST_LOCATION_LONGITUDE)),Option.empty,NoteState(COLLABORATION_STATE_DOING, Some(COLLABORATION_USER_PERU)),None),
      SimpleNote(None,COLLABORATION_SECOND_NOTE_CONTENT,Some(new DateTime()),Some(Location(COLLABORATION_SECOND_LOCATION_LATITUDE,COLLABORATION_SECOND_LOCATION_LONGITUDE)),None,NoteState(COLLABORATION_STATE_DONE, Option(COLLABORATION_USER_PERU)),None)))
  )

  override def afterAll(): Unit = {
    ActorContainer.shutdown()
    TestKit.shutdownActorSystem(system)
  }

  "A DBWorkerCollaborations actor" should {
    "insert new collaboration in the db" in {

      within(TestUtil.TASK_WAIT_TIME second) {
        Await.result(rootYellowPages ? ActorRequestMessage(Topic() :+ Database :+ Collaboration, DefaultWorker), askTimeout.duration)
          .asInstanceOf[ActorResponseMessage] match {
          case response: ActorResponseOKMessage =>
            response.actor ! InsertCollaborationMessage(collaboration, TestUtil.USER_ID)
            expectMsgPF() {
              case QueryOkMessage(query) => assert(query.isInstanceOf[InsertCollaborationMessage])
            }
          case _ => fail

        }
      }
    }

    "update a collaboration in the db" in {
      within(TestUtil.TASK_WAIT_TIME second) {
        Await.result(rootYellowPages ? ActorRequestMessage(Topic() :+ Database :+ Collaboration, DefaultWorker), askTimeout.duration)
          .asInstanceOf[ActorResponseMessage] match {
            case response: ActorResponseOKMessage =>
              response.actor ! UpdateCollaborationMessage(collaboration, TestUtil.USER_ID)
              expectMsgPF() {
                case QueryOkMessage(query) => assert(query.isInstanceOf[UpdateCollaborationMessage])
              }
            case _ => fail
        }
      }
    }

    "delete a collaboration in the db" in {
      within(TestUtil.TASK_WAIT_TIME second) {
        Await.result(rootYellowPages ? ActorRequestMessage(Topic() :+ Database :+ Collaboration, DefaultWorker), askTimeout.duration)
          .asInstanceOf[ActorResponseMessage] match {
          case response: ActorResponseOKMessage =>
            response.actor ! DeleteCollaborationMessage(collaboration, TestUtil.USER_ID)
            expectMsgPF() {
              case QueryOkMessage(query) => assert(query.isInstanceOf[DeleteCollaborationMessage])
            }
          case _ => fail
        }
      }
    }
  }
}
