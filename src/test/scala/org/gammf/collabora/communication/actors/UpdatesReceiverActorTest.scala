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

class UpdatesReceiverActorTest extends TestKit (ActorSystem("CollaboraServer")) with WordSpecLike with DefaultTimeout with Matchers with BeforeAndAfterAll with ImplicitSender {

  val rabbitConnection: ActorRef = system.actorOf(ConnectionActor.props(factory), "rabbitmq")
  val naming: ActorRef = system.actorOf(Props[RabbitMQNamingActor], "naming")
  val channelCreator: ActorRef = system.actorOf(Props[ChannelCreatorActor], "channelCreator")
  val publisherActor: ActorRef = system.actorOf(Props[PublisherActor], "publisher")
  val notificationActor: ActorRef = system.actorOf(Props(new NotificationsSenderActor(rabbitConnection, naming, channelCreator, publisherActor)))
  val dbConnectionActor: ActorRef = system.actorOf(Props[ConnectionManagerActor])
  val dbActor: ActorRef = system.actorOf(Props.create(classOf[DBActor], dbConnectionActor, notificationActor))


  override def afterAll(): Unit = {
    TestKit.shutdownActorSystem(system)
  }

  "A UpdatesReceivedActor actor" should {

    "communicate with RabbitMQNamingActor" in {
      within(500 millis){
        naming ! ChannelNamesRequestMessage(CommunicationType.UPDATES)
        expectMsg(ChannelNamesResponseMessage("updates",Some("update.server")))
      }
    }

    "communicate with channelCreatorActor" in {
      within(500 millis){
        channelCreator ! SubscribingChannelCreationMessage(rabbitConnection, "updates", "update.server", None)
        expectMsgType[ChannelCreatedMessage]
      }
    }

  }


}

