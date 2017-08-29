package org.gammf.collabora.communication.actors

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.testkit.{DefaultTimeout, ImplicitSender, TestKit}
import com.newmotion.akka.rabbitmq.{ConnectionActor, ConnectionFactory}
import com.rabbitmq.client._
import org.gammf.collabora.communication.Utils.CommunicationType
import org.gammf.collabora.communication.messages._
import org.gammf.collabora.database.actors.DBMasterActor
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

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
  val notificationActor:ActorRef = system.actorOf(Props(new NotificationsSenderActor(connection, naming, channelCreator, publisher,system,collaborationMember)))
  val dbMasterActor:ActorRef = system.actorOf(Props.create(classOf[DBMasterActor], system, notificationActor,collaborationMember))
  val subscriber:ActorRef = system.actorOf(Props[SubscriberActor], "subscriber")
  val updatesReceiver :ActorRef= system.actorOf(Props(
    new UpdatesReceiverActor(connection, naming, channelCreator, subscriber, dbMasterActor)), "updates-receiver")

  var msgCollab,msgNotif: String = ""


  override def beforeAll(): Unit = {
      fakeReceiver("collaborations","maffone","localhost")
      fakeReceiver("notifications","123456788698540008900400","localhost")
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
      within(5 seconds){
        naming ! ChannelNamesRequestMessage(CommunicationType.COLLABORATIONS)
        expectMsg(ChannelNamesResponseMessage("collaborations", None))
      }
    }

    "communicate with channelCreatorActor" in {
      within(5 seconds){
        channelCreator ! PublishingChannelCreationMessage(connection, "collaborations", None)
        expectMsgType[ChannelCreatedMessage]
      }
    }

    "send collaboration to user that have just added and a notification to all the old member of collaboration" in {
      val message = "{\"messageType\": \"CREATION\",\"target\" : \"MEMBER\",\"user\" : \"maffone\",\"member\": {\"user\": \"maffone\",\"right\": \"WRITE\"},\"collaborationId\":\"123456788698540008900400\"}"
      notificationActor ! StartMessage
      collaborationMember ! StartMessage
      updatesReceiver ! StartMessage
      updatesReceiver ! ClientUpdateMessage(message)
      eventually{
        msgNotif should not be ""
        msgCollab should not be ""
      }
      System.out.println(msgCollab)
      System.out.println(msgNotif)
      assert(msgNotif.startsWith("{\"target\":\"MEMBER\",\"messageType\":\"CREATION\",\"user\":\"maffone\",\"member\"")
            && msgCollab.startsWith("{\"user\":\"maffone\",\"collaboration\":{\"id\":\"123456788698540008900400\",\"name\":\"simplecollaboration\",\"collaborationType\":\"GROUP\""))
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
        val tmpMsg = new String(body, "UTF-8")
        if (tmpMsg.startsWith("{\"target\":\"MEMBER\",\"messageType\":\"CREATION\",\"user\":\"maffone\",\"member\"")) msgNotif = tmpMsg
        else msgCollab = tmpMsg
      }
    }
    channel.basicConsume(queueName, true, consumer)
  }




}
