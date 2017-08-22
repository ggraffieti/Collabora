package org.gammf.collabora.communication.actors

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.testkit.{DefaultTimeout, ImplicitSender, TestKit}
import com.newmotion.akka.rabbitmq.{ ConnectionActor, ConnectionFactory}
import org.gammf.collabora.communication.Utils.CommunicationType
import org.gammf.collabora.communication.messages._
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}
import play.api.libs.json.{JsValue, Json}

import scala.concurrent.duration._

class CollaborationMembersActorTest extends TestKit (ActorSystem("CollaboraServer")) with WordSpecLike with DefaultTimeout with Matchers with BeforeAndAfterAll with ImplicitSender {

  val factory = new ConnectionFactory()
  val connection:ActorRef = system.actorOf(ConnectionActor.props(factory), "rabbitmq")
  val naming: ActorRef = system.actorOf(Props[RabbitMQNamingActor], "naming")
  val channelCreator: ActorRef = system.actorOf(Props[ChannelCreatorActor], "channelCreator")
  val publisher: ActorRef = system.actorOf(Props[PublisherActor], "publisher")
  val collaborationMember: ActorRef = system.actorOf(Props(
    new CollaborationMembersActor(connection, naming, channelCreator, publisher)), "collaboration-members")
  val subscriber:ActorRef = system.actorOf(Props[SubscriberActor], "subscriber")


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


  override def afterAll(): Unit = {
    TestKit.shutdownActorSystem(system)
  }

  "A CollaborationMember actor" should {

    "communicate with RabbitMQNamingActor" in {
      within(500 millis){
        naming ! ChannelNamesRequestMessage(CommunicationType.COLLABORATIONS)
        expectMsg(ChannelNamesResponseMessage("collaborations", None))
      }
    }

    "communicate with channelCreatorActor" in {
      within(500 millis){
        channelCreator ! PublishingChannelCreationMessage(connection, "collaborations", None)
        expectMsgType[ChannelCreatedMessage]
      }
    }

    "sends all the information needed by a user that has just been added to a collaboration" in {
      collaborationMember ! PublishMemberAddedMessage("maffone", message)
      collaborationMember ! StartMessage

    }


  }


}
