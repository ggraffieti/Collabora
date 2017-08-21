package org.gammf.collabora.communication.actors

import java.util

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.testkit.{DefaultTimeout, ImplicitSender, TestKit}
import com.newmotion.akka.rabbitmq.{Channel, Connection, ConnectionActor, ConnectionFactory}
import org.gammf.collabora.Test.{dbConnectionActor, factory, notificationActor, system}
import org.gammf.collabora.communication.Utils.CommunicationType
import org.gammf.collabora.communication.actors.UseCollaborationMembersActor.{channelCreator, collaborationMember, connection, naming, system}
import org.gammf.collabora.communication.messages._
import org.gammf.collabora.database.actors.{ConnectionManagerActor, DBActor}
import org.gammf.collabora.database.messages.InsertNoteMessage
import org.gammf.collabora.util.UpdateMessage
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}
import play.api.libs.json.{JsValue, Json}

import scala.collection.mutable
import scala.collection.mutable.ListBuffer
import scala.concurrent.duration._

class PublisherActorTest extends TestKit (ActorSystem("CollaboraServer")) with WordSpecLike with DefaultTimeout with Matchers with BeforeAndAfterAll with ImplicitSender {

  val factory = new ConnectionFactory()
  val connection: ActorRef = system.actorOf(ConnectionActor.props(factory), "rabbitmq")
  val naming: ActorRef = system.actorOf(Props[RabbitMQNamingActor], "naming")
  val channelCreator: ActorRef = system.actorOf(Props[ChannelCreatorActor], "channelCreator")
  val publisher:ActorRef = system.actorOf(Props[PublisherActor], "publisher")
  val collaborationMember:ActorRef = system.actorOf(Props(
    new CollaborationMembersActor(connection, naming, channelCreator, publisher)), "collaboration-members")
  val message : JsValue = Json.parse("""
  {
      "user": "manuelperuzzi",
      "collaboration": {
        "id": "arandomidofarandomcollaboration",
        "name": "random-collaboration",
        "collaborationType": "group",
        "users": [
          {
            "username": "manuelperuzzi",
            "email": "manuel.peruzzi@studio.unibo.it",
            "name": "Manuel",
            "surname": "Peruzzi",
            "right": "admin"
          }
        ],
        "notes": [
          {
            "id": "arandomidofarandomnote",
            "content": "some content",
            "state": {
              "definition": "doing",
              "username": "manuelperuzzi"
            }
          }
        ]
      }
  }""")


  override def beforeAll(): Unit ={
    collaborationMember ! PublishMemberAddedMessage("maffone", message)
    collaborationMember ! PublishMemberAddedMessage("pinuzzo", message)
  }

  override def afterAll(): Unit = {
    TestKit.shutdownActorSystem(system)
  }


  "A Publish actor" should {

    "receive all messages only when channel is created correctly" in {
      val messages = mutable.MutableList[PublishMessage]()
      within(500 millis) {
        collaborationMember ! StartMessage
        expectMsgType[PublishMessage]
        //expectMsgType[PublishMessage]
        receiveWhile(500 millis){
          case msg: PublishMessage => messages += msg
        }
      }
      messages.length should be(2)

    }

  }






}
