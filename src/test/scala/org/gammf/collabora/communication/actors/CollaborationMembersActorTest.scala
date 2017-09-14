package org.gammf.collabora.communication.actors

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.testkit.{DefaultTimeout, ImplicitSender, TestKit}
import com.newmotion.akka.rabbitmq.{ConnectionActor, ConnectionFactory}
import com.rabbitmq.client._
import org.gammf.collabora.{TestMessageUtil, TestUtil}
import org.gammf.collabora.communication.Utils.CommunicationType
import org.gammf.collabora.communication.messages._
import org.gammf.collabora.database.actors.master.DBMasterActor
import org.gammf.collabora.yellowpages.ActorService.ConnectionHandler
import org.gammf.collabora.yellowpages.actors.YellowPagesActor
import org.gammf.collabora.yellowpages.messages.RegistrationRequestMessage
import org.gammf.collabora.yellowpages.util.Topic
import org.gammf.collabora.yellowpages.TopicElement._
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

import scala.concurrent.duration._
import org.scalatest.concurrent.Eventually


class CollaborationMembersActorTest extends TestKit (ActorSystem("CollaboraServer")) with WordSpecLike with Eventually with DefaultTimeout with Matchers with BeforeAndAfterAll with ImplicitSender {
/*
  val PUBLISHER_ACTOR_NAME = "PublisherActor"
  val COLLABORATION_MEMBER_ACTOR_NAME = "CollaborationActor"
  val SUBSCRIBER_ACTOR_NAME = "SubscriberActor"
  val NOTIFICATION_ACTOR_NAME = "NotificationActor"
  val UPDATES_RECEIVER_ACTOR_NAME = "UpdatesReceiver"
  val DBMASTER_ACTOR_NAME = "DBMaster"
  val CONNECTION_ACTOR_NAME = "RabbitConnection"
  val NAMING_ACTOR_NAME = "NamingActor"
  val CHANNEL_CREATOR_NAME = "RabbitChannelCreator"

  val rootYellowPages = system.actorOf(YellowPagesActor.rootProps())

  val factory = new ConnectionFactory()
  val rabbitConnection = system.actorOf(ConnectionActor.props(factory), CONNECTION_ACTOR_NAME)
  rootYellowPages ! RegistrationRequestMessage(rabbitConnection, CONNECTION_ACTOR_NAME, Topic() :+ Communication :+ RabbitMQ, ConnectionHandler)

  val channelCreator = system.actorOf(ChannelCreatorActor.printerProps(rootYellowPages, Topic() :+ Communication :+ RabbitMQ, CHANNEL_CREATOR_NAME))
  val namingActor = system.actorOf(RabbitMQNamingActor.printerProps(rootYellowPages, Topic() :+ Communication :+ RabbitMQ, NAMING_ACTOR_NAME))
  val publisherActor = system.actorOf(PublisherActor.printerProps(rootYellowPages, Topic() :+ Communication :+ RabbitMQ, PUBLISHER_ACTOR_NAME))
  val subscriber = system.actorOf(SubscriberActor.printerProps(rootYellowPages, Topic() :+ Communication :+ RabbitMQ, SUBSCRIBER_ACTOR_NAME))
  val updatesReceiver = system.actorOf(UpdatesReceiverActor.printerProps(rootYellowPages, Topic() :+ Communication :+ Updates :+ RabbitMQ , UPDATES_RECEIVER_ACTOR_NAME))
  val notificationActor = system.actorOf(NotificationsSenderActor.printerProps(rootYellowPages, Topic() :+ Communication :+ Notifications :+ RabbitMQ, NOTIFICATION_ACTOR_NAME))
  val collaborationActor = system.actorOf(CollaborationMembersActor.printerProps(rootYellowPages, Topic() :+ Communication :+ Collaborations  :+ RabbitMQ, COLLABORATION_MEMBER_ACTOR_NAME))
  val dbMasterActor = system.actorOf(DBMasterActor.printerProps(rootYellowPages, Topic() :+ Database, DBMASTER_ACTOR_NAME))

  var msgCollab,msgNotif: String = ""


  override def beforeAll(): Unit = {
      fakeReceiver(TestUtil.TYPE_COLLABORATIONS,TestUtil.COLLABORATION_ROUTING_KEY, TestUtil.BROKER_HOST)
      fakeReceiver(TestUtil.TYPE_NOTIFICATIONS,TestUtil.NOTIFICATIONS_ROUTING_KEY, TestUtil.BROKER_HOST)
  }

  override def afterAll(): Unit = {
    TestKit.shutdownActorSystem(system)
  }

  override implicit val patienceConfig: PatienceConfig = PatienceConfig(
    timeout = scaled(TestUtil.TIMEOUT_SECOND seconds),
    interval = scaled(TestUtil.INTERVAL_MILLIS millis)
  )

  "A CollaborationMember actor" should {

    "communicate with RabbitMQNamingActor" in {
      within(TestUtil.TASK_WAIT_TIME seconds){
        namingActor ! ChannelNamesRequestMessage(CommunicationType.COLLABORATIONS)
        expectMsg(ChannelNamesResponseMessage(TestUtil.TYPE_COLLABORATIONS, None))
      }
    }

    "communicate with channelCreatorActor" in {
      within(TestUtil.TASK_WAIT_TIME seconds){
        channelCreator ! PublishingChannelCreationMessage(TestUtil.TYPE_COLLABORATIONS, None)
        expectMsgType[ChannelCreatedMessage]
      }
    }

    "send collaboration to user that have just added and a notification to all the old member of collaboration" in {
      val message = TestMessageUtil.collaborationMembersActorTestMessage
      notificationActor ! StartMessage
      collaborationActor ! StartMessage
      updatesReceiver ! StartMessage
      updatesReceiver ! ClientUpdateMessage(message)
      eventually{
        msgNotif should not be ""
        msgCollab should not be ""
      }
      System.out.println(msgCollab)
      System.out.println(msgNotif)
      assert(msgNotif.startsWith(TestMessageUtil.startMsgNotifCollaborationMembersActorTest)
            && msgCollab.startsWith(TestMessageUtil.startMsgCollabCollaborationMembersActorTest))
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
        val tmpMsg = new String(body, TestUtil.STRING_ENCODING)
        if (tmpMsg.startsWith(TestMessageUtil.startMsgNotifCollaborationMembersActorTest)) msgNotif = tmpMsg
        else msgCollab = tmpMsg
      }
    }
    channel.basicConsume(queueName, true, consumer)
  }

*/
}
