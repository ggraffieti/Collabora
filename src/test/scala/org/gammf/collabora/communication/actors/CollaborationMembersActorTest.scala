package org.gammf.collabora.communication.actors

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.testkit.{DefaultTimeout, ImplicitSender, TestKit}
import com.newmotion.akka.rabbitmq.{ConnectionActor, ConnectionFactory}
import com.rabbitmq.client._
import org.gammf.collabora.{TestMessageUtil, TestUtil}
import org.gammf.collabora.communication.Utils.CommunicationType
import org.gammf.collabora.communication.messages._
import org.gammf.collabora.database.actors.master.DBMasterActor
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

import scala.concurrent.duration._
import org.scalatest.concurrent.Eventually


class CollaborationMembersActorTest extends TestKit (ActorSystem("CollaboraServer")) with WordSpecLike with Eventually with DefaultTimeout with Matchers with BeforeAndAfterAll with ImplicitSender {

  val CONNECTION_ACTOR_NAME = "rabbitmq"
  val NAMING_ACTOR_NAME = "naming"
  val CHANNEL_CREATOR_NAME = "channelCreator"
  val PUBLISHER_ACTOR_NAME = "publisher"
  val COLLABORATION_MEMBER_ACTOR_NAME = "collaboration-members"
  val SUBSCRIBER_ACTOR_NAME = "subscriber"
  val UPDATES_RECEIVER_ACTOR_NAME = "updates-receiver"

  val factory = new ConnectionFactory()
  val connection:ActorRef = system.actorOf(ConnectionActor.props(factory), CONNECTION_ACTOR_NAME)
  val naming: ActorRef = system.actorOf(Props[RabbitMQNamingActor], NAMING_ACTOR_NAME)
  val channelCreator: ActorRef = system.actorOf(Props[ChannelCreatorActor], CHANNEL_CREATOR_NAME)
  val publisher: ActorRef = system.actorOf(Props[PublisherActor], PUBLISHER_ACTOR_NAME)
  val collaborationMember: ActorRef = system.actorOf(Props(
    new CollaborationMembersActor(connection, naming, channelCreator, publisher)), COLLABORATION_MEMBER_ACTOR_NAME)
  val notificationActor:ActorRef = system.actorOf(Props(new NotificationsSenderActor(connection, naming, channelCreator, publisher,system)))
  val dbMasterActor:ActorRef = system.actorOf(Props.create(classOf[DBMasterActor], system, notificationActor,collaborationMember))
  val subscriber:ActorRef = system.actorOf(Props[SubscriberActor], SUBSCRIBER_ACTOR_NAME)
  val updatesReceiver :ActorRef= system.actorOf(Props(
    new UpdatesReceiverActor(connection, naming, channelCreator, subscriber, dbMasterActor)), UPDATES_RECEIVER_ACTOR_NAME)

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
        naming ! ChannelNamesRequestMessage(CommunicationType.COLLABORATIONS)
        expectMsg(ChannelNamesResponseMessage(TestUtil.TYPE_COLLABORATIONS, None))
      }
    }

    "communicate with channelCreatorActor" in {
      within(TestUtil.TASK_WAIT_TIME seconds){
        channelCreator ! PublishingChannelCreationMessage(connection, TestUtil.TYPE_COLLABORATIONS, None)
        expectMsgType[ChannelCreatedMessage]
      }
    }

    "send collaboration to user that have just added and a notification to all the old member of collaboration" in {
      val message = TestMessageUtil.collaborationMembersActorTestMessage
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




}
