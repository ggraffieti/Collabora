package org.gammf.collabora.database.actors

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit}
import com.newmotion.akka.rabbitmq.{ConnectionActor, ConnectionFactory}
import com.rabbitmq.client.{AMQP, BuiltinExchangeType, DefaultConsumer, Envelope}
import org.gammf.collabora.{TestMessageUtil, TestUtil}
import org.gammf.collabora.communication.actors._
import org.gammf.collabora.communication.messages.{ClientUpdateMessage, StartMessage}
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

import scala.concurrent.duration._
import org.scalatest.concurrent.Eventually


class DBMasterActorTest extends TestKit (ActorSystem("CollaboraServer")) with WordSpecLike with Eventually with Matchers with BeforeAndAfterAll with ImplicitSender {

  val CONNECTION_ACTOR_NAME = "rabbitmq"
  val NAMING_ACTOR_NAME = "naming"
  val CHANNEL_CREATOR_NAME = "channelCreator"
  val PUBLISHER_ACTOR_NAME = "publisher"
  val UPDATES_RECEIVER_ACTOR_NAME = "updates-receiver"
  val SUBSCRIBER_ACTOR_NAME = "subscriber"

  val dbConnectionActor: ActorRef = system.actorOf(Props[ConnectionManagerActor])
  val factory = new ConnectionFactory()
  val connection:ActorRef = system.actorOf(ConnectionActor.props(factory), CONNECTION_ACTOR_NAME)
  val naming: ActorRef = system.actorOf(Props[RabbitMQNamingActor], NAMING_ACTOR_NAME)
  val channelCreator: ActorRef = system.actorOf(Props[ChannelCreatorActor], CHANNEL_CREATOR_NAME)
  val publisherActor: ActorRef = system.actorOf(Props[PublisherActor], PUBLISHER_ACTOR_NAME)
  val collaborationMemberActor:ActorRef = system.actorOf(Props(
    new CollaborationMembersActor(connection, naming, channelCreator, publisherActor)))
  val notificationActor: ActorRef = system.actorOf(Props(new NotificationsSenderActor(connection, naming, channelCreator, publisherActor,system)))
  val dbMasterActor:ActorRef = system.actorOf(Props.create(classOf[DBMasterActor], system, notificationActor,collaborationMemberActor))
  val subscriber:ActorRef = system.actorOf(Props[SubscriberActor], SUBSCRIBER_ACTOR_NAME)
  val updatesReceiver:ActorRef = system.actorOf(Props(
    new UpdatesReceiverActor(connection, naming, channelCreator, subscriber, dbMasterActor)), UPDATES_RECEIVER_ACTOR_NAME)

  var msg: String = ""

  override def beforeAll(): Unit = {
    val factory = new ConnectionFactory
    factory.setHost(TestUtil.BROKER_HOST)
    val connection = factory.newConnection
    val channel = connection.createChannel
    channel.exchangeDeclare(TestUtil.TYPE_NOTIFICATIONS, BuiltinExchangeType.DIRECT, true)
    val queueName = channel.queueDeclare.getQueue
    channel.queueBind(queueName, TestUtil.TYPE_NOTIFICATIONS, TestUtil.ROUTING_KEY)
    val consumer = new DefaultConsumer(channel) {
      override def handleDelivery(consumerTag: String, envelope: Envelope, properties: AMQP.BasicProperties, body: Array[Byte]): Unit = {
        msg = new String(body, TestUtil.STRING_ENCODING)
      }
    }
    channel.basicConsume(queueName, true, consumer)

    updatesReceiver ! StartMessage
    notificationActor ! StartMessage

  }
  override def afterAll(): Unit = {
    TestKit.shutdownActorSystem(system)
  }

  override implicit val patienceConfig: PatienceConfig = PatienceConfig(
    timeout = scaled(TestUtil.TIMEOUT_SECOND seconds),
    interval = scaled(TestUtil.INTERVAL_MILLIS millis)
  )

  "A DBMaster actor" should {

    "act like a gateway for every request from and to the DB" in {
      val message = TestMessageUtil.messageDBMasterActorTest
      updatesReceiver ! ClientUpdateMessage(message)
      eventually{
        msg should not be ""
      }
      val contain = "target\":\"NOTE\",\"messageType\" : \"CREATION"
      assert(msg.contains(contain))
    }
  }

}
