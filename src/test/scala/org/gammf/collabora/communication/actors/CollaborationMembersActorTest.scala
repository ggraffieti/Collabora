package org.gammf.collabora.communication.actors

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.testkit.{DefaultTimeout, ImplicitSender, TestKit}
import com.newmotion.akka.rabbitmq.{ConnectionActor, ConnectionFactory}
import com.rabbitmq.client._
import org.gammf.collabora.communication.Utils.CommunicationType
import org.gammf.collabora.communication.messages._
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}
import play.api.libs.json.{JsObject, JsString, JsValue, Json}

import scala.concurrent.duration._
import org.scalatest.concurrent.Eventually


class CollaborationMembersActorTest extends TestKit (ActorSystem("CollaboraServer")) with WordSpecLike with Eventually with DefaultTimeout with Matchers with BeforeAndAfterAll with ImplicitSender {

  private val EXCHANGE_NAME = "collaborations"
  private val ROUTING_KEY = "maffone"
  private val BROKER_HOST = "localhost"

  val factory = new ConnectionFactory()
  val connection:ActorRef = system.actorOf(ConnectionActor.props(factory), "rabbitmq")
  val naming: ActorRef = system.actorOf(Props[RabbitMQNamingActor], "naming")
  val channelCreator: ActorRef = system.actorOf(Props[ChannelCreatorActor], "channelCreator")
  val publisher: ActorRef = system.actorOf(Props[PublisherActor], "publisher")
  val collaborationMember: ActorRef = system.actorOf(Props(
    new CollaborationMembersActor(connection, naming, channelCreator, publisher)), "collaboration-members")

  var msg: String = ""
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

  override def beforeAll(): Unit = {
      val factory = new ConnectionFactory
      factory.setHost(BROKER_HOST)
      val connection = factory.newConnection
      val channel = connection.createChannel
      channel.exchangeDeclare(EXCHANGE_NAME, BuiltinExchangeType.DIRECT, true)
      val queueName = channel.queueDeclare.getQueue
      channel.queueBind(queueName, EXCHANGE_NAME, ROUTING_KEY)
      val consumer = new DefaultConsumer(channel) {
        override def handleDelivery(consumerTag: String, envelope: Envelope, properties: AMQP.BasicProperties, body: Array[Byte]): Unit = {
          msg = new String(body, "UTF-8")
          //System.out.println(" [x] Received '" + msg + "'")
        }
      }
      channel.basicConsume(queueName, true, consumer)
  }

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

    "check message sended and recived is the same" in {
      eventually{
        msg should not be ""
      }
      assert(msg.equals(message.toString()))
      msg = ""
    }

    "don't recive messages if user is not part of the collaboration" in {
      collaborationMember ! PublishMemberAddedMessage("peru", message)
      eventually{
        msg should be ("")
      }
    }




  }


}
