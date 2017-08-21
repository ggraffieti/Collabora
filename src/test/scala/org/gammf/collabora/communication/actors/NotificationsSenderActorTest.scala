package org.gammf.collabora.communication.actors

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.testkit.{DefaultTimeout, ImplicitSender, TestKit}
import com.newmotion.akka.rabbitmq.{Channel, Connection, ConnectionActor, ConnectionFactory}
import org.gammf.collabora.Test.{dbConnectionActor, factory, notificationActor, system}
import org.gammf.collabora.communication.Utils.CommunicationType
import org.gammf.collabora.communication.messages._
import org.gammf.collabora.database.actors.{ConnectionManagerActor, DBActor}
import org.gammf.collabora.database.messages.InsertNoteMessage
import org.gammf.collabora.util.UpdateMessage
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}
import play.api.libs.json.{JsValue, Json}

import scala.concurrent.duration._

class NotificationsSenderActorTest extends TestKit (ActorSystem("CollaboraServer")) with WordSpecLike with DefaultTimeout with Matchers with BeforeAndAfterAll with ImplicitSender {

  val factory = new ConnectionFactory()
  val connection:ActorRef = system.actorOf(ConnectionActor.props(factory), "rabbitmq")
  val naming: ActorRef = system.actorOf(Props[RabbitMQNamingActor], "naming")
  val channelCreator: ActorRef = system.actorOf(Props[ChannelCreatorActor], "channelCreator")

  override def beforeAll(): Unit ={

  }

  override def afterAll(): Unit = {
    TestKit.shutdownActorSystem(system)
  }

  "A NotificationsSender actor" should {

    "communicate with RabbitMQNamingActor" in {
      within(500 millis){
        naming ! ChannelNamesRequestMessage(CommunicationType.NOTIFICATIONS)
        expectMsg(ChannelNamesResponseMessage("notifications", None))
      }
    }

    "communicate with channelCreatorActor" in {
      within(500 millis){
        channelCreator ! PublishingChannelCreationMessage(connection, "notifications", None)
        expectMsgType[ChannelCreatedMessage]
      }
    }


  }




}
