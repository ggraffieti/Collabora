package org.gammf.collabora.communication.actors

import akka.actor.{ActorRef, ActorSystem}
import akka.testkit.{DefaultTimeout, ImplicitSender, TestKit}
import akka.util.Timeout
import akka.pattern.ask
import com.newmotion.akka.rabbitmq.ConnectionFactory
import com.rabbitmq.client._
import org.gammf.collabora.communication.messages.PublishMessage
import org.gammf.collabora.{TestMessageUtil, TestUtil}
import org.gammf.collabora.yellowpages.ActorContainer
import org.gammf.collabora.yellowpages.ActorService._
import org.gammf.collabora.yellowpages.messages.{ActorRequestMessage, ActorResponseMessage, ActorResponseOKMessage}
import org.gammf.collabora.yellowpages.util.Topic
import org.gammf.collabora.yellowpages.TopicElement._
import org.scalatest.concurrent.Eventually
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}
import play.api.libs.json.Json

import scala.concurrent.Await
import scala.concurrent.duration._
import org.scalatest.concurrent.Eventually._
import org.scalatest.time.{Millis, Seconds, Span}

import scala.language.postfixOps

class RabbitMQPublisherActorTest extends TestKit (ActorSystem("CollaboraTest")) with WordSpecLike with DefaultTimeout with Matchers with BeforeAndAfterAll with ImplicitSender {

  implicit protected[this] val askTimeout: Timeout = Timeout(5 second)
  implicit val patienceConfig: Eventually.PatienceConfig =
    PatienceConfig(timeout = scaled(Span(2, Seconds)), interval = scaled(Span(5, Millis)))

  var stringCollaborationMessage = ""
  var stringNotificationMessage = ""

  var connection: Connection = _
  var notificationsChannel: Channel = _
  var collaborationsChannel: Channel = _
  var rootYellowPages: ActorRef = _

  override def beforeAll(): Unit = {
    ActorContainer.init()
    ActorContainer.createAll()
    rootYellowPages = ActorContainer.rootYellowPages

    val factory = new ConnectionFactory
    factory.setHost(TestUtil.BROKER_HOST)
    connection = factory.newConnection
    createFakeCollaborationReceiver(TestUtil.TYPE_COLLABORATIONS, TestUtil.COLLABORATION_ROUTING_KEY)
    createFakeNotificationsReceiver(TestUtil.TYPE_NOTIFICATIONS, TestMessageUtil.collaborationId)

    Thread.sleep(200)
  }

  override def afterAll(): Unit = {
    ActorContainer.shutdown()
    TestKit.shutdownActorSystem(system)
  }


  "A Publisher actor" should {

    "correctly publish an update message in exchange \"notifications\"" in {
      val message = PublishMessage(notificationsChannel, TestUtil.TYPE_NOTIFICATIONS, Some(TestMessageUtil.collaborationId), Json.toJson("test string"))

      Await.result(rootYellowPages ? ActorRequestMessage(Topic() :+ Communication :+ RabbitMQ, Publishing), askTimeout.duration)
        .asInstanceOf[ActorResponseMessage] match {
        case response: ActorResponseOKMessage => response.actor ! message
        case _ => fail
      }
      eventually {
        stringNotificationMessage should not be ""
      }
      assert(stringNotificationMessage == Json.toJson("test string").toString)
    }

    "correcly publish an collaboration message in exchange \"collaborations\"" in {
      val message = PublishMessage(collaborationsChannel, TestUtil.TYPE_COLLABORATIONS, Some(TestUtil.COLLABORATION_ROUTING_KEY), Json.toJson("another test string"))

      Await.result(rootYellowPages ? ActorRequestMessage(Topic() :+ Communication :+ RabbitMQ, Publishing), askTimeout.duration)
        .asInstanceOf[ActorResponseMessage] match {
        case response: ActorResponseOKMessage => response.actor ! message
        case _ => fail
      }
      eventually {
        stringCollaborationMessage should not be ""
      }
      assert(stringCollaborationMessage == Json.toJson("another test string").toString)
    }
  }

  private def createFakeNotificationsReceiver(exchangeName: String, routingKey: String): Unit = {
    notificationsChannel = connection.createChannel
    notificationsChannel.exchangeDeclare(exchangeName, BuiltinExchangeType.DIRECT, true)
    val queueName = notificationsChannel.queueDeclare.getQueue
    notificationsChannel.queueBind(queueName, exchangeName, routingKey)
    val consumer = new DefaultConsumer(notificationsChannel) {

      override def handleDelivery(consumerTag: String, envelope: Envelope, properties: AMQP.BasicProperties, body: Array[Byte]): Unit = {
        stringNotificationMessage = new String(body, TestUtil.STRING_ENCODING)
      }
    }
    notificationsChannel.basicConsume(queueName, true, consumer)
  }

  private def createFakeCollaborationReceiver(exchangeName: String, routingKey: String): Unit = {
    collaborationsChannel = connection.createChannel
    collaborationsChannel.exchangeDeclare(exchangeName, BuiltinExchangeType.DIRECT, true)
    val queueName = collaborationsChannel.queueDeclare.getQueue
    collaborationsChannel.queueBind(queueName, exchangeName, routingKey)
    val consumer = new DefaultConsumer(collaborationsChannel) {

      override def handleDelivery(consumerTag: String, envelope: Envelope, properties: AMQP.BasicProperties, body: Array[Byte]): Unit = {
        stringCollaborationMessage = new String(body, TestUtil.STRING_ENCODING)
      }
    }
    collaborationsChannel.basicConsume(queueName, true, consumer)
  }
}
