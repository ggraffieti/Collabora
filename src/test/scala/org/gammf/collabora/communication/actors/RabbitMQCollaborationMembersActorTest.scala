package org.gammf.collabora.communication.actors

import akka.actor.{ActorRef, ActorSystem}
import akka.pattern.ask
import akka.testkit.{DefaultTimeout, ImplicitSender, TestKit}
import akka.util.Timeout
import com.newmotion.akka.rabbitmq.ConnectionFactory
import com.rabbitmq.client._
import org.gammf.collabora.communication.CommunicationType
import org.gammf.collabora.{TestMessageUtil, TestUtil}
import org.gammf.collabora.communication.messages._
import org.gammf.collabora.yellowpages.ActorContainer
import org.gammf.collabora.yellowpages.ActorService._
import org.gammf.collabora.yellowpages.messages._
import org.gammf.collabora.yellowpages.util.Topic
import org.gammf.collabora.yellowpages.TopicElement._
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

import scala.concurrent.duration._
import org.scalatest.concurrent.Eventually

import scala.concurrent.Await
import scala.language.postfixOps

class RabbitMQCollaborationMembersActorTest extends TestKit (ActorSystem("CollaboraTest")) with WordSpecLike with Eventually with DefaultTimeout with Matchers with BeforeAndAfterAll with ImplicitSender {

  var msgCollab, msgNotif: String = ""
  implicit protected[this] val askTimeout: Timeout = Timeout(5 second)

  var connection: Connection= _
  var rootYellowPages: ActorRef = _


  override def beforeAll(): Unit = {
    ActorContainer.init()
    ActorContainer.createAll()
    rootYellowPages = ActorContainer.rootYellowPages

    val factory = new ConnectionFactory
    factory.setHost(TestUtil.BROKER_HOST)
    connection = factory.newConnection

    createFakeReceiver(TestUtil.TYPE_COLLABORATIONS, TestUtil.COLLABORATION_ROUTING_KEY)
    createFakeReceiver(TestUtil.TYPE_NOTIFICATIONS, TestUtil.NOTIFICATIONS_ROUTING_KEY)
    Thread.sleep(200)
  }

  override def afterAll(): Unit = {
    ActorContainer.shutdown()
    TestKit.shutdownActorSystem(system)
  }

  override implicit val patienceConfig: PatienceConfig = PatienceConfig(
    timeout = scaled(TestUtil.TIMEOUT_SECOND seconds),
    interval = scaled(TestUtil.INTERVAL_MILLIS millis)
  )

  "A CollaborationMemberActor" should {

    "communicate with RabbitMQNamingActor a receive a proper response message" in {
      within(TestUtil.TASK_WAIT_TIME seconds) {
        Await.result(rootYellowPages ? ActorRequestMessage(Topic() :+ Communication :+ RabbitMQ, Naming), askTimeout.duration)
          .asInstanceOf[ActorResponseMessage] match{
          case response: ActorResponseOKMessage =>
            response.actor ! ChannelNamesRequestMessage(CommunicationType.COLLABORATIONS)
            expectMsgPF() {
              case ChannelNamesResponseMessage(name, None) => assert(name.equals("collaborations"))
            }
          case _ => fail
        }
      }
    }

    "communicate with channelCreatorActor" in {
      within(TestUtil.TASK_WAIT_TIME seconds) {
        Await.result(rootYellowPages ? ActorRequestMessage(Topic() :+ Communication :+ RabbitMQ, ChannelCreating), askTimeout.duration)
          .asInstanceOf[ActorResponseMessage] match {
          case response:
            ActorResponseOKMessage => response.actor ! PublishingChannelCreationMessage(TestUtil.TYPE_COLLABORATIONS, None)
            expectMsgPF() {
              case ChannelCreatedMessage(channel) => assert(channel.isOpen)
            }
          case _ => fail
        }
      }
    }

    "send collaboration to user that have just added and a notification to all the old member of collaboration" in {
      val message = TestMessageUtil.collaborationMembersActorTestMessage

      Await.result(rootYellowPages ? ActorRequestMessage(Topic() :+ Communication :+ Updates :+ RabbitMQ, Master), askTimeout.duration)
        .asInstanceOf[ActorResponseMessage] match {
        case response: ActorResponseOKMessage => response.actor ! ClientUpdateMessage(message)
        case _ => fail
      }
      eventually {
        msgNotif should not be ""
        msgCollab should not be ""
      }
      assert(msgNotif.startsWith(TestMessageUtil.startMsgNotifCollaborationMembersActorTest)
        && msgCollab.startsWith(TestMessageUtil.startMsgCollabCollaborationMembersActorTest))
    }
  }

  def createFakeReceiver(exchangeName: String, routingKey: String): Unit = {
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
}
