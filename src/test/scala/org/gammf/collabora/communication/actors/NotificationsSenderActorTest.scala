package org.gammf.collabora.communication.actors

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.testkit.{DefaultTimeout, ImplicitSender, TestKit}
import com.newmotion.akka.rabbitmq.{ConnectionActor, ConnectionFactory}
import com.rabbitmq.client.{ConnectionFactory, _}
import org.gammf.collabora.EntryPoint.{notificationActor, system}
import org.gammf.collabora.communication.Utils.CommunicationType
import org.gammf.collabora.communication.messages._
import org.gammf.collabora.database.actors.{ConnectionManagerActor, DBMasterActor}
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

import scala.concurrent.duration._
import org.scalatest.concurrent.Eventually

class NotificationsSenderActorTest extends TestKit (ActorSystem("CollaboraServer")) with WordSpecLike with Eventually with DefaultTimeout with Matchers with BeforeAndAfterAll with ImplicitSender {

  val CONNECTION_ACTOR_NAME = "rabbitmq"
  val NAMING_ACTOR_NAME = "naming"
  val CHANNEL_CREATOR_NAME = "channelCreator"
  private val PUBLISHER_ACTOR_NAME = "publisher"
  val SUBSCRIBER_ACTOR_NAME = "subscriber"
  val UPDATES_RECEIVER_ACTOR_NAME = "updates-receiver"

  val STRING_ENCODING = "UTF-8"

  private val EXCHANGE_NAME = "notifications"
  private val ROUTING_KEY = "59806a4af27da3fcfe0ac0ca"
  private val BROKER_HOST = "localhost"
  val TIMEOUT_SECOND = 4
  val INTERVAL_MILLIS = 100;

  val TASK_WAIT_TIME = 5;

  val factory = new ConnectionFactory()
  val connection:ActorRef = system.actorOf(ConnectionActor.props(factory), CONNECTION_ACTOR_NAME)
  val naming:ActorRef = system.actorOf(Props[RabbitMQNamingActor], NAMING_ACTOR_NAME)
  val channelCreator :ActorRef= system.actorOf(Props[ChannelCreatorActor], CHANNEL_CREATOR_NAME)
  val publisherActor:ActorRef = system.actorOf(Props[PublisherActor], PUBLISHER_ACTOR_NAME)
  val collaborationMemberActor:ActorRef = system.actorOf(Props(
    new CollaborationMembersActor(connection, naming, channelCreator, publisherActor)))
  val notificationActor:ActorRef = system.actorOf(Props(
    new NotificationsSenderActor(connection, naming, channelCreator, publisherActor,system)))
  val dbConnectionActor :ActorRef= system.actorOf(Props[ConnectionManagerActor])
  val dbMasterActor:ActorRef = system.actorOf(Props.create(classOf[DBMasterActor], system, notificationActor,collaborationMemberActor))
  val subscriber:ActorRef = system.actorOf(Props[SubscriberActor], SUBSCRIBER_ACTOR_NAME)
  val updatesReceiver :ActorRef= system.actorOf(Props(
    new UpdatesReceiverActor(connection, naming, channelCreator, subscriber, dbMasterActor)), UPDATES_RECEIVER_ACTOR_NAME)

  var msg: String = ""


  override def beforeAll(): Unit ={
    val factory = new ConnectionFactory
    factory.setHost(BROKER_HOST)
    val connection = factory.newConnection
    val channel = connection.createChannel
    channel.exchangeDeclare(EXCHANGE_NAME, BuiltinExchangeType.DIRECT, true)
    val queueName = channel.queueDeclare.getQueue
    channel.queueBind(queueName, EXCHANGE_NAME, ROUTING_KEY)
    val consumer = new DefaultConsumer(channel) {
      override def handleDelivery(consumerTag: String, envelope: Envelope, properties: AMQP.BasicProperties, body: Array[Byte]): Unit = {
        msg = new String(body, STRING_ENCODING)
      }
    }
    channel.basicConsume(queueName, true, consumer)

  }

  override def afterAll(): Unit = {
    TestKit.shutdownActorSystem(system)
  }

  override implicit val patienceConfig: PatienceConfig = PatienceConfig(
    timeout = scaled(TIMEOUT_SECOND seconds),
    interval = scaled(INTERVAL_MILLIS millis)
  )

  "A NotificationsSender actor" should {

    "communicate with RabbitMQNamingActor" in {
      within(TASK_WAIT_TIME seconds){
        naming ! ChannelNamesRequestMessage(CommunicationType.NOTIFICATIONS)
        expectMsg(ChannelNamesResponseMessage(EXCHANGE_NAME, None))
      }
    }

    "communicate with channelCreatorActor" in {
      within(TASK_WAIT_TIME seconds){
        channelCreator ! PublishingChannelCreationMessage(connection, EXCHANGE_NAME, None)
        expectMsgType[ChannelCreatedMessage]
      }
    }

    "notify clients when there are updates on db" in {
      val message = "{\"messageType\": \"CREATION\",\"collaborationId\":\"59806a4af27da3fcfe0ac0ca\",\"target\" : \"NOTE\",\"user\" : \"maffone\",\"note\": {\"content\" : \"prova test\",\"expiration\" : \"2017-08-07T08:01:17.171+02:00\",\"location\" : { \"latitude\" : 546, \"longitude\" : 324 },\"previousNotes\" : [ \"5980710df27da3fcfe0ac88e\", \"5980710df27da3fcfe0ac88f\" ],\"state\" : { \"definition\" : \"done\", \"responsible\" : \"maffone\"}}}"
      updatesReceiver ! StartMessage
      notificationActor ! StartMessage
      updatesReceiver ! ClientUpdateMessage(message)
      eventually{
        msg should not be ""
      }
      val startMsg = "{\"target\":\"NOTE\",\"messageType\":\"CREATION\",\"user\":\"maffone\",\"note\":{\"id\":"
      val endMsg = "\"content\":\"prova test\",\"expiration\":\"2017-08-07T08:01:17.171+02:00\",\"location\":{\"latitude\":546,\"longitude\":324},\"previousNotes\":[\"5980710df27da3fcfe0ac88e\",\"5980710df27da3fcfe0ac88f\"],\"state\":{\"definition\":\"done\",\"responsible\":\"maffone\"}},\"collaborationId\":\"59806a4af27da3fcfe0ac0ca\"}"

      assert(msg.startsWith(startMsg)&& msg.endsWith(endMsg))
    }



  }




}
