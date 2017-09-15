package org.gammf.collabora

import akka.actor.{ActorSystem, Props}
import com.newmotion.akka.rabbitmq.{ConnectionActor, ConnectionFactory}
import org.gammf.collabora.authentication.AuthenticationServer
import org.gammf.collabora.authentication.actors.AuthenticationActor
import org.gammf.collabora.communication.actors._
import org.gammf.collabora.communication.actors.rabbitmq._
import org.gammf.collabora.database.actors.ConnectionManagerActor
import org.gammf.collabora.database.actors.master._
import org.gammf.collabora.database.actors.worker._
import org.gammf.collabora.yellowpages.actors.{PrinterActor, YellowPagesActor}
import org.gammf.collabora.yellowpages.util.Topic
import org.gammf.collabora.yellowpages.TopicElement._
import org.gammf.collabora.yellowpages.ActorService._
import org.gammf.collabora.yellowpages.messages.{HierarchyRequestMessage, RegistrationRequestMessage}
import org.gammf.collabora.authentication.LOCALHOST_ADDRESS

object EntryPoint extends App {
  val system = ActorSystem("CollaboraServer")

  val rootYellowPages = system.actorOf(YellowPagesActor.rootProps())

  //COMMUNICATION-------------------------------------------------
  val factory = new ConnectionFactory()
  val rabbitConnection = system.actorOf(ConnectionActor.props(factory), "rabbitmq")
  rootYellowPages ! RegistrationRequestMessage(rabbitConnection, "RabbitConnection", Topic() :+ Communication :+ RabbitMQ, ConnectionHandler)

  val channelCreator = system.actorOf(Props(
    new RabbitMQChannelCreatorActor(rootYellowPages, "RabbitChannelCreator", Topic() :+ Communication :+ RabbitMQ, ChannelCreating)), "channelCreator")
  val namingActor = system.actorOf(Props(
    new RabbitMQNamingActor(rootYellowPages, "NamingActor", Topic() :+ Communication :+ RabbitMQ, Naming)), "naming")
  val publisherActor = system.actorOf(Props(
    new RabbitMQPublisherActor(rootYellowPages, "PublisherActor", Topic() :+ Communication :+ RabbitMQ, Publishing)), "publisher")
  val subscriber = system.actorOf(Props(
    new RabbitMQSubscriberActor(rootYellowPages, "SubscriberActor", Topic() :+ Communication :+ RabbitMQ, Subscribing)), "subscriber")

  val updatesReceiver = system.actorOf(Props
  (new RabbitMQUpdatesReceiverActor(rootYellowPages, "UpdatesReceiver", Topic() :+ Communication :+ Updates :+ RabbitMQ , Master)), "updates-receiver")
  val notificationActor = system.actorOf(Props(
    new RabbitMQNotificationsSenderActor(rootYellowPages, "NotificationActor", Topic() :+ Communication :+ Notifications :+ RabbitMQ, Master)), "notifications-actor")
  val collaborationActor = system.actorOf(Props(
    new RabbitMQCollaborationMembersActor(rootYellowPages, "CollaborationActor", Topic() :+ Communication :+ Collaborations  :+ RabbitMQ, Master)), "collaborations-acotr")

  val notificationDispatcherActor = system.actorOf(Props(
    new NotificationsDispatcherActor(rootYellowPages, "NotificationDispatcher", Topic() :+ Communication :+ Notifications, Bridging)
  ))

  val firebaseActor = system.actorOf(Props(
    new FirebaseActor(rootYellowPages, "FirebaseActor", Topic() :+ Communication :+ Notifications :+ Firebase, Master)
  ))




  //DB------------------------------------------

  //MONGO CONNECTION MANAGER
  val mongoConnectionActor = system.actorOf(Props(
    new ConnectionManagerActor(rootYellowPages, "MongoConnectionManager", Topic() :+ Database, ConnectionHandler)
  ))

  //MASTERS
  val dbMasterActor = system.actorOf(Props(
    new DBMasterActor(rootYellowPages, "DBMaster", Topic() :+ Database, Master)
  ))
  val dbMasterNoteActor = system.actorOf(Props(
    new DBMasterNote(rootYellowPages, "DBMasterNotes", Topic() :+ Database :+ Note, Master)
  ))
  val dbMasterModuleActor = system.actorOf(Props(
    new DBMasterModule(rootYellowPages, "DBMasterModules", Topic() :+ Database :+ Module, Master)
  ))
  val dbMasterCollaborationActor = system.actorOf(Props(
    new DBMasterCollaboration(rootYellowPages, "DBMasterCollaborations", Topic() :+ Database :+ Collaboration, Master)
  ))
  val dbMasterMemberActor = system.actorOf(Props(
    new DBMasterMember(rootYellowPages, "DBMasterMembers", Topic() :+ Database :+ Member, Master)
  ))

  //DEFAULT WORKERS
  val dBWorkerNotesActor = system.actorOf(Props(
    new DBWorkerNoteActor(rootYellowPages, "DBWorkerNotes", Topic() :+ Database :+ Note, DefaultWorker)
  ))
  val dBWorkerModulesActor = system.actorOf(Props(
    new DBWorkerModuleActor(rootYellowPages, "DBWorkerModules", Topic() :+ Database :+ Module, DefaultWorker)
  ))
  val dBWorkerCollaborationsActor = system.actorOf(Props(
    new DBWorkerCollaborationActor(rootYellowPages, "DBWorkerCollaborations", Topic() :+ Database :+ Collaboration, DefaultWorker)
  ))
  val dbWorkerMembersActor = system.actorOf(Props(
    new DBWorkerMemberActor(rootYellowPages, "DBWorkerMembers", Topic() :+ Database :+ Member, DefaultWorker)
  ))

  //EXTRA WORKERS
  val dBWorkerAuthenticationActor = system.actorOf(Props(
    new DBWorkerAuthenticationActor(rootYellowPages, "DBWorkerAuthentication", Topic() :+ Database, Authenticator)
  ))
  val dBWorkerChangeModuleStateActor = system.actorOf(Props(
    new DBWorkerChangeModuleStateActor(rootYellowPages, "DBWorkerChangeModuleState", Topic() :+ Database :+ Module, StateChanger)
  ))
  val dBWorkerCheckMemberExistenceActor = system.actorOf(Props(
    new DBWorkerCheckMemberExistenceActor(rootYellowPages, "DBWorkerCheckMember", Topic() :+ Database :+ Member, ExistenceChecking)
  ))
  val dBWorkerGetCollaborationActor = system.actorOf(Props(
    new DBWorkerGetCollaborationActor(rootYellowPages, "DBWorkerGetCollaboration", Topic() :+ Database :+ Collaboration, Getter)
  ))


  //AUTHENTICATION
  val authenticationActor = system.actorOf(Props(
    new AuthenticationActor(rootYellowPages, "authenticationActor", Topic() :+ Authentication, Bridging)
  ))

  AuthenticationServer.start(system, authenticationActor, if (args.length == 0) LOCALHOST_ADDRESS else args(0))

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

  Thread.sleep(1000)

  rootYellowPages ! HierarchyRequestMessage(0)
}
