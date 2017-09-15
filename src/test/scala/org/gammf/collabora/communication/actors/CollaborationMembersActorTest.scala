package org.gammf.collabora.communication.actors

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.testkit.{DefaultTimeout, ImplicitSender, TestKit}
import com.newmotion.akka.rabbitmq.{ConnectionActor, ConnectionFactory}
import com.rabbitmq.client._
import org.gammf.collabora.EntryPoint.actorCreator
import org.gammf.collabora.authentication.AuthenticationServer
import org.gammf.collabora.authentication.actors.AuthenticationActor
import org.gammf.collabora.{TestMessageUtil, TestUtil}
import org.gammf.collabora.communication.Utils.CommunicationType
import org.gammf.collabora.communication.messages._
import org.gammf.collabora.database.actors.ConnectionManagerActor
import org.gammf.collabora.database.actors.master._
import org.gammf.collabora.database.actors.worker._
import org.gammf.collabora.yellowpages.ActorCreator
import org.gammf.collabora.yellowpages.ActorService.ConnectionHandler
import org.gammf.collabora.yellowpages.actors.{PrinterActor, YellowPagesActor}
import org.gammf.collabora.yellowpages.messages.{RegistrationRequestMessage, RegistrationResponseMessage}
import org.gammf.collabora.yellowpages.util.Topic
import org.gammf.collabora.yellowpages.TopicElement._
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

import scala.concurrent.duration._
import org.scalatest.concurrent.Eventually


class CollaborationMembersActorTest extends TestKit (ActorSystem("CollaboraServer")) with WordSpecLike with Eventually with DefaultTimeout with Matchers with BeforeAndAfterAll with ImplicitSender {

  val PUBLISHER_ACTOR_NAME = "PublisherActor"
  val COLLABORATION_MEMBER_ACTOR_NAME = "CollaborationActor"
  val SUBSCRIBER_ACTOR_NAME = "SubscriberActor"
  val NOTIFICATION_ACTOR_NAME = "NotificationActor"
  val UPDATES_RECEIVER_ACTOR_NAME = "UpdatesReceiver"
  val DBMASTER_ACTOR_NAME = "DBMaster"
  val CONNECTION_ACTOR_NAME = "RabbitConnection"
  val NAMING_ACTOR_NAME = "NamingActor"
  val CHANNEL_CREATOR_NAME = "RabbitChannelCreator"

  val actorCreator = new ActorCreator(system)
  val rootYellowPages = actorCreator.getYellowPagesRoot

  val factory = new ConnectionFactory()
  val rabbitConnection = system.actorOf(ConnectionActor.props(factory), CONNECTION_ACTOR_NAME)
  rootYellowPages ! RegistrationRequestMessage(rabbitConnection, CONNECTION_ACTOR_NAME, Topic() :+ Communication :+ RabbitMQ, ConnectionHandler)

  val channelCreator = system.actorOf(ChannelCreatorActor.printerProps(rootYellowPages, Topic() :+ Communication :+ RabbitMQ, "RabbitChannelCreator"))
  val namingActor = system.actorOf(RabbitMQNamingActor.printerProps(rootYellowPages, Topic() :+ Communication :+ RabbitMQ, "NamingActor"))
  val publisherActor = system.actorOf(PublisherActor.printerProps(rootYellowPages, Topic() :+ Communication :+ RabbitMQ, "PublisherActor"))
  val subscriber = system.actorOf(SubscriberActor.printerProps(rootYellowPages, Topic() :+ Communication :+ RabbitMQ, "SubscriberActor"))

  val updatesReceiver = system.actorOf(UpdatesReceiverActor.printerProps(rootYellowPages, Topic() :+ Communication :+ Updates :+ RabbitMQ , "UpdatesReceiver"))
  val notificationActor = system.actorOf(NotificationsSenderActor.printerProps(rootYellowPages, Topic() :+ Communication :+ Notifications :+ RabbitMQ, "NotificationActor"))
  val collaborationActor = system.actorOf(CollaborationMembersActor.printerProps(rootYellowPages, Topic() :+ Communication :+ Collaborations  :+ RabbitMQ, "CollaborationActor"))

  val notificationDispatcherActor = system.actorOf(NotificationsDispatcherActor.printerProps(rootYellowPages, Topic() :+ Communication :+ Notifications, "NotificationDispatcher"))

  val firebaseActor = system.actorOf(FirebaseActor.printerProps(rootYellowPages, Topic() :+ Communication :+ Notifications :+ Firebase, "FirebaseActor"))

  //MONGO CONNECTION MANAGER
  val mongoConnectionActor = system.actorOf(ConnectionManagerActor.printerProps(rootYellowPages, Topic() :+ Database, "MongoConnectionManager"))

  //MASTERS
  val dbMasterActor = system.actorOf(DBMasterActor.printerProps(rootYellowPages, Topic() :+ Database, "DBMaster"))
  val dbMasterNoteActor = system.actorOf(DBMasterNote.printerProps(rootYellowPages, Topic() :+ Database :+ Note, "DBMasterNotes"))
  val dbMasterModuleActor = system.actorOf(DBMasterModule.printerProps(rootYellowPages, Topic() :+ Database :+ Module, "DBMasterModules"))
  val dbMasterCollaborationActor = system.actorOf(DBMasterCollaboration.printerProps(rootYellowPages, Topic() :+ Database :+ Collaboration, "DBMasterCollaborations"))
  val dbMasterMemberActor = system.actorOf(DBMasterMember.printerProps(rootYellowPages, Topic() :+ Database :+ Member, "DBMasterMembers"))

  //DEFAULT WORKERS
  val dBWorkerNotesActor = system.actorOf(DBWorkerNotesActor.printerProps(rootYellowPages, Topic() :+ Database :+ Note, "DBWorkerNotes"))
  val dBWorkerModulesActor = system.actorOf(DBWorkerModulesActor.printerProps(rootYellowPages, Topic() :+ Database :+ Module, "DBWorkerModules"))
  val dBWorkerCollaborationsActor = system.actorOf(DBWorkerCollaborationsActor.printerProps(rootYellowPages, Topic() :+ Database :+ Collaboration, "DBWorkerCollaborations"))
  val dbWorkerMembersActor = system.actorOf(DBWorkerMemberActor.printerProps(rootYellowPages, Topic() :+ Database :+ Member, "DBWorkerMembers"))

  //EXTRA WORKERS
  val dBWorkerAuthenticationActor = system.actorOf(DBWorkerAuthenticationActor.printerProps(rootYellowPages, Topic() :+ Database, "DBWorkerAuthentication"))
  val dBWorkerChangeModuleStateActor = system.actorOf(DBWorkerChangeModuleStateActor.printerProps(rootYellowPages, Topic() :+ Database :+ Module, "DBWorkerChangeModuleState"))
  val dBWorkerCheckMemberExistenceActor = system.actorOf(DBWorkerCheckMemberExistenceActor.printerProps(rootYellowPages, Topic() :+ Database :+ Member, "DBWorkerCheckMember"))
  val dBWorkerGetCollaborationActor = system.actorOf(DBWorkerGetCollaborationActor.printerProps(rootYellowPages, Topic() :+ Database :+ Collaboration, "DBWorkerGetCollaboration"))

  val authenticationActor = system.actorOf(AuthenticationActor.printerProps(rootYellowPages, Topic() :+ Authentication, "AuthenticationActor"))
  AuthenticationServer.start(system, authenticationActor)

  val printerActor = system.actorOf(PrinterActor
    .printerProps(rootYellowPages, Topic() :+ General))

  val yellowPagesCommunication = system.actorOf(YellowPagesActor
    .topicProps(rootYellowPages, Topic() :+ Communication))

  val yellowPagesDatabase = system.actorOf(YellowPagesActor
    .topicProps(rootYellowPages, Topic() :+ Database))

  val yellowPagesCommunicationRabbit = system.actorOf(YellowPagesActor
    .topicProps(rootYellowPages, Topic() :+ Communication :+ RabbitMQ))

  val yellowPagesDatabaseNote = system.actorOf(YellowPagesActor
    .topicProps(rootYellowPages, Topic() :+ Database :+ Note))
  val yellowPagesDatabaseModule = system.actorOf(YellowPagesActor
    .topicProps(rootYellowPages, Topic() :+ Database :+ Module))
  val yellowPagesDatabaseCollaboration = system.actorOf(YellowPagesActor
    .topicProps(rootYellowPages, Topic() :+ Database :+ Collaboration))
  val yellowPagesDatabaseMember = system.actorOf(YellowPagesActor
    .topicProps(rootYellowPages, Topic() :+ Database :+ Member))

  var msgCollab,msgNotif: String = ""


  override def beforeAll(): Unit = {
    System.out.println("1");
      fakeReceiver(TestUtil.TYPE_COLLABORATIONS,TestUtil.COLLABORATION_ROUTING_KEY, TestUtil.BROKER_HOST)
    System.out.println("2");
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
        expectMsg(RegistrationResponseMessage())
      }
    }

    "communicate with channelCreatorActor" in {
      within(TestUtil.TASK_WAIT_TIME seconds){
        channelCreator ! PublishingChannelCreationMessage(TestUtil.TYPE_COLLABORATIONS, None)
        expectMsg(ChannelNamesResponseMessage(TestUtil.TYPE_COLLABORATIONS, None))
      }
    }

    "send collaboration to user that have just added and a notification to all the old member of collaboration" in {
      val message = TestMessageUtil.collaborationMembersActorTestMessage
      //notificationActor ! StartMessage
      //collaborationActor ! StartMessage
      //updatesReceiver ! StartMessage
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
