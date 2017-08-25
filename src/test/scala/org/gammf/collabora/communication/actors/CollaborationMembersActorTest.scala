package org.gammf.collabora.communication.actors

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.testkit.{DefaultTimeout, ImplicitSender, TestKit}
import com.newmotion.akka.rabbitmq.{ConnectionActor, ConnectionFactory}
import com.rabbitmq.client._
import org.gammf.collabora.communication.Utils.CommunicationType
import org.gammf.collabora.communication.messages._
import org.gammf.collabora.database.actors.{ConnectionManagerActor, DBMasterActor}
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}
import play.api.libs.json.{JsObject, JsString, JsValue, Json}

import scala.concurrent.duration._
import org.scalatest.concurrent.Eventually


class CollaborationMembersActorTest extends TestKit (ActorSystem("CollaboraServer")) with WordSpecLike with Eventually with DefaultTimeout with Matchers with BeforeAndAfterAll with ImplicitSender {

  val factory = new ConnectionFactory()
  val connection:ActorRef = system.actorOf(ConnectionActor.props(factory), "rabbitmq")
  val naming: ActorRef = system.actorOf(Props[RabbitMQNamingActor], "naming")
  val channelCreator: ActorRef = system.actorOf(Props[ChannelCreatorActor], "channelCreator")
  val publisher: ActorRef = system.actorOf(Props[PublisherActor], "publisher")
  val collaborationMember: ActorRef = system.actorOf(Props(
    new CollaborationMembersActor(connection, naming, channelCreator, publisher)), "collaboration-members")
  val notificationActor:ActorRef = system.actorOf(Props(new NotificationsSenderActor(connection, naming, channelCreator, publisher)))
  val dbMasterActor:ActorRef = system.actorOf(Props.create(classOf[DBMasterActor], system, notificationActor,collaborationMember))
  val subscriber:ActorRef = system.actorOf(Props[SubscriberActor], "subscriber")
  val updatesReceiver :ActorRef= system.actorOf(Props(
    new UpdatesReceiverActor(connection, naming, channelCreator, subscriber, dbMasterActor)), "updates-receiver")

  var msg: String = ""


  override def beforeAll(): Unit = {
      fakeReceiver("collaboration","maffone","localhost")
      fakeReceiver("notifications","123456788698540008900000","localhost")
  }

  override def afterAll(): Unit = {
    TestKit.shutdownActorSystem(system)
  }

  override implicit val patienceConfig: PatienceConfig = PatienceConfig(
    timeout = scaled(2 seconds),
    interval = scaled(100 millis)
  )

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

    "sends all the information needed by a user that has just created a collaboration" in {
      /*val message = "{\"messageType\": \"CREATION\",\"target\" : \"COLLABORATION\",\"user\" : \"maffone\",\"collaboration\": {\"name\": \"provatest\",\"collaborationType\": \"GROUP\",\"users\":[{ \"user\":\"maffone\",\"right\":\"ADMIN\"}]}}"
      updatesReceiver ! StartMessage
      notificationActor ! StartMessage
      collaborationMember ! StartMessage
      updatesReceiver ! ClientUpdateMessage(message)
      eventually{
        msg should not be ""
      }
      System.out.println(msg)
      assert(msg.startsWith("{\"user\":\"maffone\",\"collaboration\")"))*/
    }

    "send collaboration to user that have just added and a notification to all the old member of collaboration" in {
      val message = "{\"messageType\": \"CREATION\",\"target\" : \"MEMBER\",\"user\" : \"maffone\",\"member\": {\"user\": \"maffone\",\"right\": \"WRITE\"},\"collaborationId\":\"123456788698540008900000\"}"
      notificationActor ! StartMessage
      collaborationMember ! StartMessage
      updatesReceiver ! StartMessage
      updatesReceiver ! ClientUpdateMessage(message)
      eventually{
        msg should not be ""
      }
      //System.out.println(msg)
      //assert(msg.startsWith("{\"user\":\"maffone\",\"collaboration\")"))*/
      /*eventually{
        msg should not be ""
      }
      assert(msg.equals(message.toString()))
      msg = ""*/
    }

    "don't recive messages if user is not part of the collaboration" in {
      /*collaborationMember ! PublishMemberAddedMessage("peru", message)
      eventually{
        msg should be ("")
      }*/
    }




  }

  def fakeReceiver(exchangeName:String, routingKey:String, brokerHost:String):Unit = {
    val factory = new ConnectionFactory
    factory.setHost(brokerHost)
    val connection = factory.newConnection
    val channel = connection.createChannel
    channel.exchangeDeclare(exchangeName, BuiltinExchangeType.DIRECT, true)
    val queueName = channel.queueDeclare.getQueue
    channel.queueBind(queueName, exchangeName, routingKey)
    val consumer = new DefaultConsumer(channel) {
      override def handleDelivery(consumerTag: String, envelope: Envelope, properties: AMQP.BasicProperties, body: Array[Byte]): Unit = {
        msg = new String(body, "UTF-8")
        System.out.println(exchangeName+ " "+ msg)
      }
    }
    channel.basicConsume(queueName, true, consumer)
  }




}
