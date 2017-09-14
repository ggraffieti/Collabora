package org.gammf.collabora.communication.actors

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.testkit.{DefaultTimeout, ImplicitSender, TestKit}
import com.newmotion.akka.rabbitmq.{ConnectionActor, ConnectionFactory}
import com.rabbitmq.client.{ConnectionFactory, _}
import org.gammf.collabora.EntryPoint.system
import org.gammf.collabora.{TestMessageUtil, TestUtil}
import org.gammf.collabora.communication.Utils.CommunicationType
import org.gammf.collabora.communication.messages._
import org.gammf.collabora.database.actors.ConnectionManagerActor
import org.gammf.collabora.database.actors.master.DBMasterActor
import org.gammf.collabora.yellowpages.ActorCreator
import org.gammf.collabora.yellowpages.ActorService.ConnectionHandler
import org.gammf.collabora.yellowpages.actors.YellowPagesActor
import org.gammf.collabora.yellowpages.messages.{RegistrationRequestMessage, RegistrationResponseMessage}
import org.gammf.collabora.yellowpages.util.Topic
import org.gammf.collabora.yellowpages.TopicElement._
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

import scala.concurrent.duration._
import org.scalatest.concurrent.Eventually

class NotificationsSenderActorTest extends TestKit (ActorSystem("CollaboraServer")) with WordSpecLike with Eventually with DefaultTimeout with Matchers with BeforeAndAfterAll with ImplicitSender {

  private val EXCHANGE_NAME = "notifications"
  private val ROUTING_KEY = "59806a4af27da3fcfe0ac0ca"
  private val BROKER_HOST = "localhost"

  val PUBLISHER_ACTOR_NAME = "PublisherActor"
  val COLLABORATION_MEMBER_ACTOR_NAME = "CollaborationActor"
  val SUBSCRIBER_ACTOR_NAME = "SubscriberActor"
  val NOTIFICATION_ACTOR_NAME = "NotificationActor"
  val UPDATES_RECEIVER_ACTOR_NAME = "UpdatesReceiver"
  val DBMASTER_ACTOR_NAME = "DBMaster"
  val CONNECTION_ACTOR_NAME = "RabbitConnection"
  val NAMING_ACTOR_NAME = "NamingActor"
  val CHANNEL_CREATOR_NAME = "RabbitChannelCreator"
  val MONGO_CONNECTION_ACTOR_NAME = "MongoConnectionManager"

  val actorCreator = new ActorCreator(system)
  val rootYellowPages = actorCreator.getYellowPagesRoot

  val factory = new ConnectionFactory()
  val rabbitConnection = system.actorOf(ConnectionActor.props(factory), CONNECTION_ACTOR_NAME)
  rootYellowPages ! RegistrationRequestMessage(rabbitConnection, CONNECTION_ACTOR_NAME, Topic() :+ Communication :+ RabbitMQ, ConnectionHandler)

  val channelCreator = system.actorOf(ChannelCreatorActor.printerProps(rootYellowPages, Topic() :+ Communication :+ RabbitMQ, CHANNEL_CREATOR_NAME))
  val namingActor = system.actorOf(RabbitMQNamingActor.printerProps(rootYellowPages, Topic() :+ Communication :+ RabbitMQ, NAMING_ACTOR_NAME))
/*  val publisherActor = system.actorOf(PublisherActor.printerProps(rootYellowPages, Topic() :+ Communication :+ RabbitMQ, PUBLISHER_ACTOR_NAME))
  val subscriber = system.actorOf(SubscriberActor.printerProps(rootYellowPages, Topic() :+ Communication :+ RabbitMQ, SUBSCRIBER_ACTOR_NAME))
  val updatesReceiver = system.actorOf(UpdatesReceiverActor.printerProps(rootYellowPages, Topic() :+ Communication :+ Updates :+ RabbitMQ , UPDATES_RECEIVER_ACTOR_NAME))
  val notificationActor = system.actorOf(NotificationsSenderActor.printerProps(rootYellowPages, Topic() :+ Communication :+ Notifications :+ RabbitMQ, NOTIFICATION_ACTOR_NAME))
  val collaborationActor = system.actorOf(CollaborationMembersActor.printerProps(rootYellowPages, Topic() :+ Communication :+ Collaborations  :+ RabbitMQ, COLLABORATION_MEMBER_ACTOR_NAME))
  val mongoConnectionActor = system.actorOf(ConnectionManagerActor.printerProps(rootYellowPages, Topic() :+ Database, MONGO_CONNECTION_ACTOR_NAME))
  val dbMasterActor = system.actorOf(DBMasterActor.printerProps(rootYellowPages, Topic() :+ Database, DBMASTER_ACTOR_NAME))
*/
  var msg: String = ""


  override def beforeAll(): Unit ={
   /* val factory = new ConnectionFactory
    factory.setHost(BROKER_HOST)
    val connection = factory.newConnection
    val channel = connection.createChannel
    channel.exchangeDeclare(EXCHANGE_NAME, BuiltinExchangeType.DIRECT, true)
    val queueName = channel.queueDeclare.getQueue
    channel.queueBind(queueName, EXCHANGE_NAME, ROUTING_KEY)
    val consumer = new DefaultConsumer(channel) {
      override def handleDelivery(consumerTag: String, envelope: Envelope, properties: AMQP.BasicProperties, body: Array[Byte]): Unit = {
        msg = new String(body, TestUtil.STRING_ENCODING)
      }
    }
    channel.basicConsume(queueName, true, consumer)
*/
  }

  override def afterAll(): Unit = {
    TestKit.shutdownActorSystem(system)
  }

  override implicit val patienceConfig: PatienceConfig = PatienceConfig(
    timeout = scaled(60 seconds),
    interval = scaled(TestUtil.INTERVAL_MILLIS millis)
  )

  "A NotificationsSender actor" should {

    "communicate with RabbitMQNamingActor" in {
      within(TestUtil.TASK_WAIT_TIME seconds){
        namingActor ! ChannelNamesRequestMessage(CommunicationType.NOTIFICATIONS)
        expectMsg(RegistrationResponseMessage())
      }
    }

    "communicate with channelCreatorActor" in {
      within(TestUtil.TASK_WAIT_TIME seconds){
        channelCreator ! PublishingChannelCreationMessage(TestUtil.TYPE_NOTIFICATIONS, None)
        expectMsg(ChannelNamesResponseMessage(TestUtil.TYPE_NOTIFICATIONS, None))
      }
    }
/*
    "notify clients when there are updates on db" in {
      val message = TestMessageUtil.messageNotificationsSenderActorTest
      updatesReceiver ! StartMessage
      notificationActor ! StartMessage
      updatesReceiver ! ClientUpdateMessage(message)
      eventually{
        msg should not be ""
      }
      val startMsg = TestMessageUtil.startMessageNotificationsSenderActorTest
      val endMsg = TestMessageUtil.endMessageNotificationsSenderActorTest
      assert(msg.startsWith(startMsg)&& msg.endsWith(endMsg))
    }
*/
  }

}
