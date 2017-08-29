package org.gammf.collabora.database.actors

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit}
import com.newmotion.akka.rabbitmq.{ConnectionActor, ConnectionFactory}
import com.rabbitmq.client.{AMQP, BuiltinExchangeType, DefaultConsumer, Envelope}
import org.gammf.collabora.communication.actors._
import org.gammf.collabora.communication.messages.{ClientUpdateMessage, StartMessage}
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

import scala.concurrent.duration._
import org.scalatest.concurrent.Eventually


class DBMasterActorTest extends TestKit (ActorSystem("CollaboraServer")) with WordSpecLike with Eventually with Matchers with BeforeAndAfterAll with ImplicitSender {

  private val EXCHANGE_NAME = "notifications"
  private val ROUTING_KEY = "59806a4af27da3fcfe0ac0ca"
  private val BROKER_HOST = "localhost"

  val dbConnectionActor: ActorRef = system.actorOf(Props[ConnectionManagerActor])
  val factory = new ConnectionFactory()
  val connection:ActorRef = system.actorOf(ConnectionActor.props(factory), "rabbitmq")
  val naming: ActorRef = system.actorOf(Props[RabbitMQNamingActor], "naming")
  val channelCreator: ActorRef = system.actorOf(Props[ChannelCreatorActor], "channelCreator")
  val publisherActor: ActorRef = system.actorOf(Props[PublisherActor], "publisher")
  val collaborationMemberActor:ActorRef = system.actorOf(Props(
    new CollaborationMembersActor(connection, naming, channelCreator, publisherActor)))
  val notificationActor: ActorRef = system.actorOf(Props(new NotificationsSenderActor(connection, naming, channelCreator, publisherActor,system)))
  val dbMasterActor:ActorRef = system.actorOf(Props.create(classOf[DBMasterActor], system, notificationActor,collaborationMemberActor))
  val subscriber:ActorRef = system.actorOf(Props[SubscriberActor], "subscriber")
  val updatesReceiver:ActorRef = system.actorOf(Props(
    new UpdatesReceiverActor(connection, naming, channelCreator, subscriber, dbMasterActor)), "updates-receiver")

  var msg: String = ""

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
    timeout = scaled(2 seconds),
    interval = scaled(100 millis)
  )

  "A DBMaster actor" should {

    "act like a gateway for every request from and to the DB" in {
      val message = "{\"messageType\": \"CREATION\",\"collaborationId\":\"59806a4af27da3fcfe0ac0ca\",\"target\" : \"NOTE\",\"user\" : \"maffone\",\"note\": {\"content\" : \"c'ho un nervoso che ti ciacherei la testa\",\"expiration\" : \"2017-08-07T08:01:17.171+02:00\",\"location\" : { \"latitude\" : 546, \"longitude\" : 324 },\"previousNotes\" : [ \"5980710df27da3fcfe0ac88e\", \"5980710df27da3fcfe0ac88f\" ],\"state\" : { \"definition\" : \"done\", \"username\" : \"maffone\"}}}"
      updatesReceiver ! ClientUpdateMessage(message)
      eventually{
        msg should not be ""
      }
      val contain = "target\":\"NOTE\",\"messageType\":\"CREATION"
      assert(msg.contains(contain))
    }
  }

}
