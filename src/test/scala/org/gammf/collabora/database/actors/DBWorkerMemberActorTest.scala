package org.gammf.collabora.database.actors

import akka.actor.{ActorRef, ActorSystem}
import akka.testkit.{ImplicitSender, TestKit}
import akka.pattern.ask
import akka.util.Timeout
import org.gammf.collabora.TestUtil
import org.gammf.collabora.database.messages._
import org.gammf.collabora.util.{CollaborationRight, CollaborationUser, SimpleUser}
import org.gammf.collabora.yellowpages.ActorContainer
import org.gammf.collabora.yellowpages.ActorService.DefaultWorker
import org.gammf.collabora.yellowpages.messages._
import org.gammf.collabora.yellowpages.util.Topic
import org.gammf.collabora.yellowpages.TopicElement._
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.language.postfixOps

class DBWorkerMemberActorTest extends TestKit (ActorSystem("CollaboraTest")) with WordSpecLike  with Matchers with BeforeAndAfterAll with ImplicitSender {

  implicit protected[this] val askTimeout: Timeout = Timeout(5 second)

  var rootYellowPages: ActorRef = _

  val COLLABORATION_USERNAME = "peru"
  val user = CollaborationUser(COLLABORATION_USERNAME, CollaborationRight.WRITE)

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

  "A DBWorkerMember actor" should {
    "insert new user in a collaboration correctly in the db" in {

      within(TestUtil.TASK_WAIT_TIME second) {
        Await.result(rootYellowPages ? ActorRequestMessage(Topic() :+ Database :+ Member, DefaultWorker), askTimeout.duration)
          .asInstanceOf[ActorResponseMessage] match {
          case response: ActorResponseOKMessage =>
            response.actor ! InsertMemberMessage(user, TestUtil.FAKE_ID, TestUtil.USER_ID)
            expectMsgPF() {
              case QueryOkMessage(query) => assert(query.isInstanceOf[InsertMemberMessage])
            }
          case _ => fail
        }
      }
    }

    "update a user right in a collaboration correctly" in {

      within(TestUtil.TASK_WAIT_TIME second) {
        Await.result(rootYellowPages ? ActorRequestMessage(Topic() :+ Database :+ Member, DefaultWorker), askTimeout.duration)
          .asInstanceOf[ActorResponseMessage] match {
          case response: ActorResponseOKMessage =>
            response.actor ! UpdateMemberMessage(user, TestUtil.FAKE_ID, TestUtil.USER_ID)
            expectMsgPF() {
              case QueryOkMessage(query) => assert(query.isInstanceOf[UpdateMemberMessage])
            }
          case _ => fail
        }
      }
    }

    "delete a user from a collaboration" in {

      within(TestUtil.TASK_WAIT_TIME second) {
        Await.result(rootYellowPages ? ActorRequestMessage(Topic() :+ Database :+ Member, DefaultWorker), askTimeout.duration)
          .asInstanceOf[ActorResponseMessage] match {
          case response: ActorResponseOKMessage =>
            response.actor ! DeleteMemberMessage(user, TestUtil.FAKE_ID, TestUtil.USER_ID)
            expectMsgPF() {
              case QueryOkMessage(query) => assert(query.isInstanceOf[DeleteMemberMessage])
            }
          case _ => fail
        }
      }
    }
  }
}

