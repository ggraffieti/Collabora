package org.gammf.collabora.database.actors

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit}
import akka.pattern.ask
import akka.util.Timeout
import com.newmotion.akka.rabbitmq.{ConnectionActor, ConnectionFactory}
import org.gammf.collabora.TestUtil
import org.gammf.collabora.communication.actors._
import org.gammf.collabora.database.actors.master.DBMasterActor
import org.gammf.collabora.database.actors.worker.DBWorkerMemberActor
import org.gammf.collabora.database.messages._
import org.gammf.collabora.util.{CollaborationRight, CollaborationUser, SimpleUser}
import org.gammf.collabora.yellowpages.ActorCreator
import org.gammf.collabora.yellowpages.ActorService.{ConnectionHandler, DefaultWorker}
import org.gammf.collabora.yellowpages.actors.YellowPagesActor
import org.gammf.collabora.yellowpages.messages._
import org.gammf.collabora.yellowpages.util.Topic
import org.gammf.collabora.yellowpages.TopicElement._
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

class DBWorkerMemberActorTest extends TestKit (ActorSystem("CollaboraServer")) with WordSpecLike  with Matchers with BeforeAndAfterAll with ImplicitSender {

  implicit protected[this] val askTimeout: Timeout = Timeout(5 second)
  val actorCreator = new ActorCreator(system)
  actorCreator.startCreation
  val rootYellowPages = actorCreator.getYellowPagesRoot

  val COLLABORATION_USERNAME = "peru"
  val user = CollaborationUser(COLLABORATION_USERNAME, CollaborationRight.WRITE)

  override def beforeAll(): Unit = {

  }

  override def afterAll(): Unit = {
    TestKit.shutdownActorSystem(system)
  }

  "A DBWorkerMember actor" should {
    "insert new user in a collaboration correctly in the db" in {

      within(TestUtil.TASK_WAIT_TIME second) {
        (rootYellowPages ? ActorRequestMessage(Topic() :+ Database :+ Member, DefaultWorker))
          .mapTo[ActorResponseMessage].map {
          case response: ActorResponseOKMessage => response.actor ! InsertMemberMessage(user, TestUtil.FAKE_ID, TestUtil.USER_ID)
          case _ =>

            expectMsg(RegistrationResponseMessage())
        }
      }
    }

    "update a user right in a collaboration correctly" in {

      within(TestUtil.TASK_WAIT_TIME second) {
        (rootYellowPages ? ActorRequestMessage(Topic() :+ Database :+ Member, DefaultWorker))
          .mapTo[ActorResponseMessage].map {
          case response: ActorResponseOKMessage => response.actor ! UpdateMemberMessage(user, TestUtil.FAKE_ID, TestUtil.USER_ID)
          case _ =>

            expectMsgType[QueryOkMessage]
        }
      }

      within(TestUtil.TASK_WAIT_TIME second) {
        (rootYellowPages ? ActorRequestMessage(Topic() :+ Database :+ Member, DefaultWorker))
          .mapTo[ActorResponseMessage].map {
          case response: ActorResponseOKMessage => response.actor ! DeleteMemberMessage(user, TestUtil.FAKE_ID, TestUtil.USER_ID)
          case _ =>

            expectMsgType[QueryOkMessage]
        }
      }
    }
  }

}

