package org.gammf.collabora.communication.actors

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.pattern.ask
import akka.testkit.{ImplicitSender, TestKit}
import akka.util.Timeout
import com.newmotion.akka.rabbitmq.{ConnectionActor, ConnectionFactory}
import org.gammf.collabora.TestUtil
import org.gammf.collabora.communication.Utils.CommunicationType
import org.gammf.collabora.communication.messages.{ChannelNamesRequestMessage, ChannelNamesResponseMessage, PublishCollaborationInCollaborationExchange}
import org.gammf.collabora.yellowpages.ActorCreator
import org.gammf.collabora.yellowpages.ActorService.{ConnectionHandler, Naming}
import org.gammf.collabora.yellowpages.actors.YellowPagesActor
import org.gammf.collabora.yellowpages.messages._
import org.gammf.collabora.yellowpages.util.Topic
import org.gammf.collabora.yellowpages.TopicElement._
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

class RabbitMQNamingActorTest extends TestKit (ActorSystem("CollaboraServer")) with WordSpecLike with Matchers with BeforeAndAfterAll with ImplicitSender {

  implicit protected[this] val askTimeout: Timeout = Timeout(5 second)

  val actorCreator = new ActorCreator(system)
  actorCreator.startCreation
  val rootYellowPages = actorCreator.getYellowPagesRoot

  override def afterAll(): Unit = {
    TestKit.shutdownActorSystem(system)
  }

  "A RabbitMQNaming actor" should {

    "handles collaboration naming requests" in {
      within(TestUtil.TASK_WAIT_TIME seconds){
        (rootYellowPages ? ActorRequestMessage(Topic() :+ Communication :+ RabbitMQ, Naming))
          .mapTo[ActorResponseMessage].map {
          case response: ActorResponseOKMessage => response.actor ! ChannelNamesRequestMessage(CommunicationType.COLLABORATIONS)
          case _ =>

            expectMsg(RegistrationResponseMessage())
        }
      }
    }

    "handles updates naming requests" in {
      within(TestUtil.TASK_WAIT_TIME seconds){
        (rootYellowPages ? ActorRequestMessage(Topic() :+ Communication :+ RabbitMQ, Naming))
          .mapTo[ActorResponseMessage].map {
          case response: ActorResponseOKMessage => response.actor ! ChannelNamesRequestMessage(CommunicationType.UPDATES)
          case _ =>

            expectMsg(ChannelNamesResponseMessage(TestUtil.TYPE_COLLABORATIONS, None))
        }
      }
    }

    "handles notification naming requests" in {
      within(TestUtil.TASK_WAIT_TIME seconds){
        (rootYellowPages ? ActorRequestMessage(Topic() :+ Communication :+ RabbitMQ, Naming))
          .mapTo[ActorResponseMessage].map {
          case response: ActorResponseOKMessage => response.actor ! ChannelNamesRequestMessage(CommunicationType.NOTIFICATIONS)
          case _ =>

            expectMsg(ChannelNamesResponseMessage(TestUtil.TYPE_UPDATES,Some(TestUtil.SERVER_UPDATE)))
        }
      }
    }
  }

}
