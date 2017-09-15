package org.gammf.collabora.communication.actors

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.pattern.ask
import akka.testkit.{DefaultTimeout, ImplicitSender, TestKit}
import akka.util.Timeout
import com.newmotion.akka.rabbitmq.{ConnectionActor, ConnectionFactory}
import com.rabbitmq.client._
import org.gammf.collabora.EntryPoint.actorCreator
import org.gammf.collabora.{TestMessageUtil, TestUtil}
import org.gammf.collabora.communication.Utils.CommunicationType
import org.gammf.collabora.communication.messages._
import org.gammf.collabora.database.actors.master.DBMasterActor
import org.gammf.collabora.yellowpages.ActorCreator
import org.gammf.collabora.yellowpages.ActorService.{ChannelCreating, ConnectionHandler, Master, Naming}
import org.gammf.collabora.yellowpages.actors.YellowPagesActor
import org.gammf.collabora.yellowpages.messages._
import org.gammf.collabora.yellowpages.util.Topic
import org.gammf.collabora.yellowpages.TopicElement._
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

import scala.concurrent.duration._
import org.scalatest.concurrent.Eventually

import scala.concurrent.ExecutionContext.Implicits.global


class CollaborationMembersActorTest extends TestKit (ActorSystem("CollaboraServer")) with WordSpecLike with Eventually with DefaultTimeout with Matchers with BeforeAndAfterAll with ImplicitSender {


  var msgCollab,msgNotif: String = ""
  implicit protected[this] val askTimeout: Timeout = Timeout(5 second)

  val actorCreator = new ActorCreator(system)
  actorCreator.startCreation
  val rootYellowPages = actorCreator.getYellowPagesRoot


  override def beforeAll(): Unit = {
  /* System.out.println("1");
      fakeReceiver(TestUtil.TYPE_COLLABORATIONS,TestUtil.COLLABORATION_ROUTING_KEY, TestUtil.BROKER_HOST)
    System.out.println("2");
      fakeReceiver(TestUtil.TYPE_NOTIFICATIONS,TestUtil.NOTIFICATIONS_ROUTING_KEY, TestUtil.BROKER_HOST)*/
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
        (rootYellowPages ? ActorRequestMessage(Topic() :+ Communication :+ RabbitMQ, Naming))
          .mapTo[ActorResponseMessage].map {
          case response: ActorResponseOKMessage => response.actor ! ChannelNamesRequestMessage(CommunicationType.COLLABORATIONS)
          case _ =>

            expectMsg(RegistrationResponseMessage())
        }
       /* namingActor ! ChannelNamesRequestMessage(CommunicationType.COLLABORATIONS)
        expectMsg(RegistrationResponseMessage())
        */
      }
    }

    "communicate with channelCreatorActor" in {
      within(TestUtil.TASK_WAIT_TIME seconds){
        (rootYellowPages ? ActorRequestMessage(Topic() :+ Communication :+ RabbitMQ, ChannelCreating))
          .mapTo[ActorResponseMessage].map {
          case response: ActorResponseOKMessage => response.actor ! PublishingChannelCreationMessage(TestUtil.TYPE_COLLABORATIONS, None)
          case _ =>

            expectMsg(ChannelNamesResponseMessage(TestUtil.TYPE_COLLABORATIONS, None))
        }
        /*
        channelCreator ! PublishingChannelCreationMessage(TestUtil.TYPE_COLLABORATIONS, None)
        expectMsg(ChannelNamesResponseMessage(TestUtil.TYPE_COLLABORATIONS, None))
        */
      }
    }

  /*   "send collaboration to user that have just added and a notification to all the old member of collaboration" in {
      val message = TestMessageUtil.collaborationMembersActorTestMessage
      //notificationActor ! StartMessage
      //collaborationActor ! StartMessage
      //updatesReceiver ! StartMessage

      //updatesReceiver ! ClientUpdateMessage(message)
      (rootYellowPages ? ActorRequestMessage(Topic() :+ Communication :+ Updates :+ RabbitMQ, Master))
        .mapTo[ActorResponseMessage].map {
        case response: ActorResponseOKMessage => response.actor ! ClientUpdateMessage(message)
        case _ =>

      }
      eventually{
        msgNotif should not be ""
        msgCollab should not be ""
      }
      System.out.println(msgCollab)
      System.out.println(msgNotif)
      assert(msgNotif.startsWith(TestMessageUtil.startMsgNotifCollaborationMembersActorTest)
            && msgCollab.startsWith(TestMessageUtil.startMsgCollabCollaborationMembersActorTest))
     }
     */

}

  def fakeReceiver(exchangeName:String, routingKey:String, brokerHost:String):Unit = {
    System.out.println("3");
    val factory = new ConnectionFactory
    factory.setHost(brokerHost)
    System.out.println("4");
    val connection = factory.newConnection
    System.out.println("5");
    val channel = connection.createChannel
    System.out.println("6");
    channel.exchangeDeclare(exchangeName, BuiltinExchangeType.DIRECT, true)
    System.out.println("7");
    val queueName = channel.queueDeclare.getQueue
    System.out.println("8");
    channel.queueBind(queueName, exchangeName, routingKey)
    System.out.println("9");
    val consumer = new DefaultConsumer(channel) {
      System.out.println("10");
      override def handleDelivery(consumerTag: String, envelope: Envelope, properties: AMQP.BasicProperties, body: Array[Byte]): Unit = {
        val tmpMsg = new String(body, TestUtil.STRING_ENCODING)
        if (tmpMsg.startsWith(TestMessageUtil.startMsgNotifCollaborationMembersActorTest)) msgNotif = tmpMsg
        else msgCollab = tmpMsg
      }
    }
    System.out.println("11");
    channel.basicConsume(queueName, true, consumer)
    System.out.println("12");
  }

}
