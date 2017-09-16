package org.gammf.collabora.communication.actors

import akka.actor.{ActorRef, ActorSystem}
import akka.testkit.{DefaultTimeout, ImplicitSender, TestKit}
import akka.pattern.ask
import akka.util.Timeout
import org.gammf.collabora.communication.CommunicationType
import org.gammf.collabora.TestUtil
import org.gammf.collabora.communication.messages._
import org.gammf.collabora.yellowpages.ActorContainer
import org.gammf.collabora.yellowpages.ActorService.{ChannelCreating, Naming}
import org.gammf.collabora.yellowpages.messages._
import org.gammf.collabora.yellowpages.util.Topic
import org.gammf.collabora.yellowpages.TopicElement._
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

import scala.concurrent.duration._
import org.scalatest.concurrent.Eventually

import scala.concurrent.ExecutionContext.Implicits.global
import scala.language.postfixOps


class RabbitMQNotificationsSenderActorTest extends TestKit (ActorSystem("CollaboraTest")) with WordSpecLike with Eventually with DefaultTimeout with Matchers with BeforeAndAfterAll with ImplicitSender {

  private val EXCHANGE_NAME = "notifications"
  private val ROUTING_KEY = "59806a4af27da3fcfe0ac0ca"
  private val BROKER_HOST = "localhost"

  var msg: String = ""
  implicit protected[this] val askTimeout: Timeout = Timeout(5 second)

  var rootYellowPages: ActorRef = _

  override def beforeAll(): Unit ={
    ActorContainer.init()
    ActorContainer.createAll()
    rootYellowPages = ActorContainer.rootYellowPages
    Thread.sleep(200)
    /*val factory = new ConnectionFactory
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
    channel.basicConsume(queueName, true, consumer)*/
  }

  override def afterAll(): Unit = {
    ActorContainer.shutdown()
    TestKit.shutdownActorSystem(system)
  }

  override implicit val patienceConfig: PatienceConfig = PatienceConfig(
    timeout = scaled(TestUtil.TIMEOUT_SECOND seconds),
    interval = scaled(TestUtil.INTERVAL_MILLIS millis)
  )

  "A NotificationsSender actor" should {

    "communicate with RabbitMQNamingActor" in {
      within(TestUtil.TASK_WAIT_TIME seconds){
        (rootYellowPages ? ActorRequestMessage(Topic() :+ Communication :+ RabbitMQ, Naming))
          .mapTo[ActorResponseMessage].map {
          case response: ActorResponseOKMessage => response.actor ! ChannelNamesRequestMessage(CommunicationType.NOTIFICATIONS)
          case _ =>

            expectMsg(RegistrationResponseMessage())
        }
      }
    }

    "communicate with channelCreatorActor" in {
      within(TestUtil.TASK_WAIT_TIME seconds){
        (rootYellowPages ? ActorRequestMessage(Topic() :+ Communication :+ RabbitMQ, ChannelCreating))
          .mapTo[ActorResponseMessage].map {
          case response: ActorResponseOKMessage => response.actor ! PublishingChannelCreationMessage(TestUtil.TYPE_NOTIFICATIONS, None)
          case _ =>

            expectMsg(ChannelNamesResponseMessage(TestUtil.TYPE_NOTIFICATIONS, None))
        }
      }
    }

  /*  "notify clients when there are updates on db" in {
      val message = TestMessageUtil.messageNotificationsSenderActorTest
      //updatesReceiver ! StartMessage
      //notificationActor ! StartMessage

      //updatesReceiver ! ClientUpdateMessage(message)
      (rootYellowPages ? ActorRequestMessage(Topic() :+ Communication :+ Updates :+ RabbitMQ, Master))
        .mapTo[ActorResponseMessage].map {
        case response: ActorResponseOKMessage => response.actor ! ClientUpdateMessage(message)
        case _ =>

      }
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
