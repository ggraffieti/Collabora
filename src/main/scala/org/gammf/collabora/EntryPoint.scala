package org.gammf.collabora

import akka.actor.{ActorSystem, Props}
import com.newmotion.akka.rabbitmq.{ConnectionActor, ConnectionFactory}
import org.gammf.collabora.authentication.AuthenticationServer
import org.gammf.collabora.authentication.actors.AuthenticationActor
import org.gammf.collabora.communication.actors._
import org.gammf.collabora.database.actors.ConnectionManagerActor
import org.gammf.collabora.database.actors.master._
import org.gammf.collabora.database.actors.worker._
import org.gammf.collabora.yellowpages.actors.{PrinterActor, YellowPagesActor}
import org.gammf.collabora.yellowpages.util.Topic
import org.gammf.collabora.yellowpages.TopicElement._
import org.gammf.collabora.yellowpages.ActorService._
import org.gammf.collabora.yellowpages.messages.{HierarchyRequestMessage, RegistrationRequestMessage}

object EntryPoint extends App {
  val system = ActorSystem("CollaboraServer")

  val rootYellowPages = system.actorOf(YellowPagesActor.rootProps())

  //COMMUNICATION-------------------------------------------------
  val factory = new ConnectionFactory()
  val rabbitConnection = system.actorOf(ConnectionActor.props(factory), "rabbitmq")
  rootYellowPages ! RegistrationRequestMessage(rabbitConnection, "RabbitConnection", Topic() :+ Communication :+ RabbitMQ, ConnectionHandler)

  val channelCreator = system.actorOf(ChannelCreatorActor.printerProps(rootYellowPages, Topic() :+ Communication :+ RabbitMQ, "RabbitChannelCreator"))
  val namingActor = system.actorOf(RabbitMQNamingActor.printerProps(rootYellowPages, Topic() :+ Communication :+ RabbitMQ, "NamingActor"))
  val publisherActor = system.actorOf(PublisherActor.printerProps(rootYellowPages, Topic() :+ Communication :+ RabbitMQ, "PublisherActor"))
  val subscriber = system.actorOf(SubscriberActor.printerProps(rootYellowPages, Topic() :+ Communication :+ RabbitMQ, "SubscriberActor"))

  val updatesReceiver = system.actorOf(UpdatesReceiverActor.printerProps(rootYellowPages, Topic() :+ Communication :+ Updates :+ RabbitMQ , "UpdatesReceiver"))
  val notificationActor = system.actorOf(NotificationsSenderActor.printerProps(rootYellowPages, Topic() :+ Communication :+ Notifications :+ RabbitMQ, "NotificationActor"))
  val collaborationActor = system.actorOf(CollaborationMembersActor.printerProps(rootYellowPages, Topic() :+ Communication :+ Collaborations  :+ RabbitMQ, "CollaborationActor"))

  val notificationDispatcherActor = system.actorOf(NotificationsDispatcherActor.printerProps(rootYellowPages, Topic() :+ Communication :+ Notifications, "NotificationDispatcher"))

  val firebaseActor = system.actorOf(FirebaseActor.printerProps(rootYellowPages, Topic() :+ Communication :+ Notifications :+ Firebase, "FirebaseActor"))


  //DB------------------------------------------

  //MONGO CONNECTION MANAGER
  val mongoConnectionActor = system.actorOf(ConnectionManagerActor.printerProps(rootYellowPages, Topic() :+ Database, "MongoConnectionManager"))

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
    new DBWorkerNotesActor(rootYellowPages, "DBWorkerNotes", Topic() :+ Database :+ Note, DefaultWorker)
  ))
  val dBWorkerModulesActor = system.actorOf(Props(
    new DBWorkerModulesActor(rootYellowPages, "DBWorkerModules", Topic() :+ Database :+ Module, DefaultWorker)
  ))
  val dBWorkerCollaborationsActor = system.actorOf(Props(
    new DBWorkerCollaborationsActor(rootYellowPages, "DBWorkerCollaborations", Topic() :+ Database :+ Collaboration, DefaultWorker)
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

  Thread.sleep(1000)

  rootYellowPages ! HierarchyRequestMessage(0)
}
