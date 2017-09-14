package org.gammf.collabora.communication.actors

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit}
import com.newmotion.akka.rabbitmq.{ConnectionActor, ConnectionFactory}
import org.gammf.collabora.TestUtil
import org.gammf.collabora.communication.Utils.CommunicationType
import org.gammf.collabora.communication.messages.{ChannelNamesRequestMessage, ChannelNamesResponseMessage, PublishCollaborationInCollaborationExchange}
import org.gammf.collabora.yellowpages.ActorService.ConnectionHandler
import org.gammf.collabora.yellowpages.actors.YellowPagesActor
import org.gammf.collabora.yellowpages.messages.{RegistrationRequestMessage, RegistrationResponseMessage}
import org.gammf.collabora.yellowpages.util.Topic
import org.gammf.collabora.yellowpages.TopicElement._
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

import scala.concurrent.duration._

class RabbitMQNamingActorTest extends TestKit (ActorSystem("CollaboraServer")) with WordSpecLike with Matchers with BeforeAndAfterAll with ImplicitSender {

  val CONNECTION_ACTOR_NAME = "RabbitConnection"
  val NAMING_ACTOR_NAME = "NamingActor"
  val CHANNEL_CREATOR_NAME = "RabbitChannelCreator"

  val rootYellowPages = system.actorOf(YellowPagesActor.rootProps())

  val factory = new ConnectionFactory()
  val rabbitConnection = system.actorOf(ConnectionActor.props(factory), CONNECTION_ACTOR_NAME)
  rootYellowPages ! RegistrationRequestMessage(rabbitConnection, CONNECTION_ACTOR_NAME, Topic() :+ Communication :+ RabbitMQ, ConnectionHandler)

  val channelCreator = system.actorOf(ChannelCreatorActor.printerProps(rootYellowPages, Topic() :+ Communication :+ RabbitMQ, CHANNEL_CREATOR_NAME))
  val namingActor = system.actorOf(RabbitMQNamingActor.printerProps(rootYellowPages, Topic() :+ Communication :+ RabbitMQ, NAMING_ACTOR_NAME))

  override def afterAll(): Unit = {
    TestKit.shutdownActorSystem(system)
  }

  "A RabbitMQNaming actor" should {

    "handles collaboration naming requests" in {
      within(TestUtil.TASK_WAIT_TIME seconds){
        namingActor ! ChannelNamesRequestMessage(CommunicationType.COLLABORATIONS)
        expectMsg(RegistrationResponseMessage())
      }
    }

    "handles updates naming requests" in {
      within(TestUtil.TASK_WAIT_TIME seconds){
        namingActor ! ChannelNamesRequestMessage(CommunicationType.UPDATES)
        expectMsg(ChannelNamesResponseMessage(TestUtil.TYPE_COLLABORATIONS, None))
      }
    }

    "handles notification naming requests" in {
      within(TestUtil.TASK_WAIT_TIME seconds){
        namingActor ! ChannelNamesRequestMessage(CommunicationType.NOTIFICATIONS)
        expectMsg(ChannelNamesResponseMessage(TestUtil.TYPE_UPDATES,Some(TestUtil.SERVER_UPDATE)))
      }
    }
  }

}
