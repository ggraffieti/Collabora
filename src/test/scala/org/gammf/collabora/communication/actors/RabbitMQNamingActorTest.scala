package org.gammf.collabora.communication.actors

import akka.actor.{ActorRef, ActorSystem}
import akka.pattern.ask
import akka.testkit.{ImplicitSender, TestKit}
import akka.util.Timeout
import org.gammf.collabora.communication.CommunicationType
import org.gammf.collabora.TestUtil
import org.gammf.collabora.communication.messages.{ChannelNamesRequestMessage, ChannelNamesResponseMessage}
import org.gammf.collabora.yellowpages.ActorContainer
import org.gammf.collabora.yellowpages.ActorService.Naming
import org.gammf.collabora.yellowpages.messages._
import org.gammf.collabora.yellowpages.util.Topic
import org.gammf.collabora.yellowpages.TopicElement._
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.language.postfixOps

class RabbitMQNamingActorTest extends TestKit (ActorSystem("CollaboraTest")) with WordSpecLike with Matchers with BeforeAndAfterAll with ImplicitSender {

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

  "A RabbitMQNaming actor" should {

    "handles collaboration naming requests" in {
      within(TestUtil.TASK_WAIT_TIME seconds){
        Await.result(rootYellowPages ? ActorRequestMessage(Topic() :+ Communication :+ RabbitMQ, Naming), askTimeout.duration)
          .asInstanceOf[ActorResponseMessage] match {
          case response:
            ActorResponseOKMessage => response.actor ! ChannelNamesRequestMessage(CommunicationType.COLLABORATIONS)
            expectMsgPF() {
              case ChannelNamesResponseMessage(channel, None) => assert(channel.equals("collaborations"))
            }
          case _ => fail
        }
      }
    }

    "handles updates naming requests" in {
      within(TestUtil.TASK_WAIT_TIME seconds){
        Await.result(rootYellowPages ? ActorRequestMessage(Topic() :+ Communication :+ RabbitMQ, Naming), askTimeout.duration)
          .asInstanceOf[ActorResponseMessage] match {
          case response:
            ActorResponseOKMessage => response.actor ! ChannelNamesRequestMessage(CommunicationType.UPDATES)
            expectMsgPF() {
              case ChannelNamesResponseMessage(channel, Some(queueName)) =>
                assert(channel.equals("updates") && queueName.equals("update.server"))
            }
          case _ => fail
        }
      }
    }

    "handles notification naming requests" in {
      within(TestUtil.TASK_WAIT_TIME seconds){
        Await.result(rootYellowPages ? ActorRequestMessage(Topic() :+ Communication :+ RabbitMQ, Naming), askTimeout.duration)
          .asInstanceOf[ActorResponseMessage] match {
          case response:
            ActorResponseOKMessage => response.actor ! ChannelNamesRequestMessage(CommunicationType.NOTIFICATIONS)
            expectMsgPF() {
              case ChannelNamesResponseMessage(channel, None) =>
                assert(channel.equals("notifications"))
            }
          case _ => fail
        }
      }
    }
  }
}
