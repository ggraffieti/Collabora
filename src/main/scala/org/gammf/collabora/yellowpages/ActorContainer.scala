package org.gammf.collabora.yellowpages

import akka.actor.{ActorRef, ActorSystem}
import com.newmotion.akka.rabbitmq.{ConnectionActor, ConnectionFactory}
import org.gammf.collabora.authentication.AuthenticationServer
import org.gammf.collabora.authentication.actors.AuthenticationActor
import org.gammf.collabora.communication.actors._
import org.gammf.collabora.database.actors.ConnectionManagerActor
import org.gammf.collabora.database.actors.master._
import org.gammf.collabora.database.actors.worker._
import org.gammf.collabora.yellowpages.ActorService.ConnectionHandler
import org.gammf.collabora.yellowpages.actors.{PrinterActor, YellowPagesActor}
import org.gammf.collabora.yellowpages.messages.RegistrationRequestMessage
import org.gammf.collabora.yellowpages.util.Topic
import org.gammf.collabora.yellowpages.TopicElement._

case class ActorCreator(system: ActorSystem) {

  val rootYellowPages = system.actorOf(YellowPagesActor.rootProps())

  def startCreation {
    val factory = new ConnectionFactory()
    val rabbitConnection = system.actorOf(ConnectionActor.props(factory), "rabbitmq")
    rootYellowPages ! RegistrationRequestMessage(rabbitConnection, "RabbitConnection", Topic() :+ Communication :+ RabbitMQ, ConnectionHandler)

    createCommunicationActor()
    createConnectionManagerActor()
    createDBMasterActors()
    createDBDefaultWorkers()
    createDBExtraWorkers()
    createAuthActor()
    createYellowPagesActor()
  }

  def createCommunicationActor() : Unit = {
    val channelCreator = system.actorOf(ChannelCreatorActor.channelCreatorProps(rootYellowPages, Topic() :+ Communication :+ RabbitMQ, "RabbitChannelCreator"))
    val namingActor = system.actorOf(RabbitMQNamingActor.namingProps(rootYellowPages, Topic() :+ Communication :+ RabbitMQ, "NamingActor"))
    val publisherActor = system.actorOf(PublisherActor.publisherProps(rootYellowPages, Topic() :+ Communication :+ RabbitMQ, "PublisherActor"))
    val subscriber = system.actorOf(SubscriberActor.subscriberProps(rootYellowPages, Topic() :+ Communication :+ RabbitMQ, "SubscriberActor"))

    val updatesReceiver = system.actorOf(UpdatesReceiverActor.updatesReceiverProps(rootYellowPages, Topic() :+ Communication :+ Updates :+ RabbitMQ , "UpdatesReceiver"))
    val notificationActor = system.actorOf(NotificationsSenderActor.notificationsSenderProps(rootYellowPages, Topic() :+ Communication :+ Notifications :+ RabbitMQ, "NotificationActor"))
    val collaborationActor = system.actorOf(CollaborationMembersActor.collaborationMemberProps(rootYellowPages, Topic() :+ Communication :+ Collaborations  :+ RabbitMQ, "CollaborationActor"))

    val notificationDispatcherActor = system.actorOf(NotificationsDispatcherActor.notificationsDispatcherProps(rootYellowPages, Topic() :+ Communication :+ Notifications, "NotificationDispatcher"))

    val firebaseActor = system.actorOf(FirebaseActor.firebaseProps(rootYellowPages, Topic() :+ Communication :+ Notifications :+ Firebase, "FirebaseActor"))
  }

  def createConnectionManagerActor() : Unit = {
    //MONGO CONNECTION MANAGER
    val mongoConnectionActor = system.actorOf(ConnectionManagerActor.connectionManagerProps(rootYellowPages, Topic() :+ Database, "MongoConnectionManager"))
  }

  def createDBMasterActors() : Unit = {
    //MASTERS
    val dbMasterActor = system.actorOf(DBMasterActor.dbMasterProps(rootYellowPages, Topic() :+ Database, "DBMaster"))
    val dbMasterNoteActor = system.actorOf(DBMasterNote.dbMasterNoteProps(rootYellowPages, Topic() :+ Database :+ Note, "DBMasterNotes"))
    val dbMasterModuleActor = system.actorOf(DBMasterModule.dbMasterModuleProps(rootYellowPages, Topic() :+ Database :+ Module, "DBMasterModules"))
    val dbMasterCollaborationActor = system.actorOf(DBMasterCollaboration.dbMasterCollaborationProps(rootYellowPages, Topic() :+ Database :+ Collaboration, "DBMasterCollaborations"))
    val dbMasterMemberActor = system.actorOf(DBMasterMember.dbMasterMemberProps(rootYellowPages, Topic() :+ Database :+ Member, "DBMasterMembers"))
  }

  def createDBDefaultWorkers() : Unit = {
    //DEFAULT WORKERS
    val dBWorkerNotesActor = system.actorOf(DBWorkerNotesActor.dbWorkerNotesProps(rootYellowPages, Topic() :+ Database :+ Note, "DBWorkerNotes"))
    val dBWorkerModulesActor = system.actorOf(DBWorkerModulesActor.dbWorkerModulesProps(rootYellowPages, Topic() :+ Database :+ Module, "DBWorkerModules"))
    val dBWorkerCollaborationsActor = system.actorOf(DBWorkerCollaborationsActor.dbWorkerCollaborationsProps(rootYellowPages, Topic() :+ Database :+ Collaboration, "DBWorkerCollaborations"))
    val dbWorkerMembersActor = system.actorOf(DBWorkerMemberActor.dbWorkerMemberProps(rootYellowPages, Topic() :+ Database :+ Member, "DBWorkerMembers"))
  }

  def createDBExtraWorkers() : Unit = {
    //EXTRA WORKERS
    val dBWorkerAuthenticationActor = system.actorOf(DBWorkerAuthenticationActor.dbWorkerAuthenticationProps(rootYellowPages, Topic() :+ Database, "DBWorkerAuthentication"))
    val dBWorkerChangeModuleStateActor = system.actorOf(DBWorkerChangeModuleStateActor.dbWorkerChangeModuleStateProps(rootYellowPages, Topic() :+ Database :+ Module, "DBWorkerChangeModuleState"))
    val dBWorkerCheckMemberExistenceActor = system.actorOf(DBWorkerCheckMemberExistenceActor.dbWorkerCheckMemberExistenceProps(rootYellowPages, Topic() :+ Database :+ Member, "DBWorkerCheckMember"))
    val dBWorkerGetCollaborationActor = system.actorOf(DBWorkerGetCollaborationActor.dbWorkerGetCollaborationProps(rootYellowPages, Topic() :+ Database :+ Collaboration, "DBWorkerGetCollaboration"))
  }

  def createAuthActor() : Unit = {
    //AUTHENTICATION
    val authenticationActor = system.actorOf(AuthenticationActor.authenticationProps(rootYellowPages, Topic() :+ Authentication, "AuthenticationActor"))
    AuthenticationServer.start(system, authenticationActor)
  }

  def createYellowPagesActor() : Unit = {

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
  }

  def getYellowPagesRoot:ActorRef = rootYellowPages

}
