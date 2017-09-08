package org.gammf.collabora.communication.actors

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit}
import com.newmotion.akka.rabbitmq.{ConnectionActor, ConnectionFactory}
import org.gammf.collabora.TestUtil
import org.gammf.collabora.communication.Utils.CommunicationType
import org.gammf.collabora.communication.messages._
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

import scala.concurrent.duration._

class UpdatesReceiverActorTest extends TestKit (ActorSystem("CollaboraServer")) with WordSpecLike  with Matchers with BeforeAndAfterAll with ImplicitSender {

  val CONNECTION_ACTOR_NAME = "rabbitmq"
  val NAMING_ACTOR_NAME = "naming"
  val CHANNEL_CREATOR_NAME = "channelCreator"

  val factory = new ConnectionFactory()
  val connection:ActorRef = system.actorOf(ConnectionActor.props(factory), CONNECTION_ACTOR_NAME)
  val naming: ActorRef = system.actorOf(Props[RabbitMQNamingActor], NAMING_ACTOR_NAME)
  val channelCreator: ActorRef = system.actorOf(Props[ChannelCreatorActor], CHANNEL_CREATOR_NAME)

  override def afterAll(): Unit = {
    TestKit.shutdownActorSystem(system)
  }

  "A UpdatesReceived actor" should {

    "start correctly" in {
      within(TestUtil.TASK_WAIT_TIME seconds){
        naming ! ChannelNamesRequestMessage(CommunicationType.UPDATES)
        expectMsg(ChannelNamesResponseMessage(TestUtil.TYPE_UPDATES,Some(TestUtil.SERVER_UPDATE)))
      }
    }

    "create channel correctly" in {
      within(TestUtil.TASK_WAIT_TIME seconds){
        channelCreator ! SubscribingChannelCreationMessage(connection, TestUtil.TYPE_UPDATES, TestUtil.SERVER_UPDATE, None)
        expectMsgType[ChannelCreatedMessage]
      }
    }



  }

}

