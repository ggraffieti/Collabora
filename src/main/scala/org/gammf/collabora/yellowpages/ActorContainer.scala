package org.gammf.collabora.yellowpages

import akka.actor.{ActorRef, ActorSystem}
import akka.util.Timeout
import com.newmotion.akka.rabbitmq.{ConnectionActor, ConnectionFactory}
import org.gammf.collabora.authentication.actors.AuthenticationActor
import org.gammf.collabora.communication.actors._
import org.gammf.collabora.communication.actors.rabbitmq._
import org.gammf.collabora.database.actors.ConnectionManagerActor
import org.gammf.collabora.database.actors.master._
import org.gammf.collabora.database.actors.worker._
import org.gammf.collabora.yellowpages.ActorService.ConnectionHandler
import org.gammf.collabora.yellowpages.actors.{PrinterActor, YellowPagesActor}
import org.gammf.collabora.yellowpages.messages.RegistrationRequestMessage
import org.gammf.collabora.yellowpages.util.Topic
import org.gammf.collabora.yellowpages.TopicElement._

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.language.postfixOps

object ActorContainer {
  private[this] val timeout: Timeout = 30 seconds

  var actorSystem: ActorSystem = _
  var rootYellowPages: ActorRef = _

  def init(): Unit = {
    actorSystem = ActorSystem("CollaboraServer")
    rootYellowPages = actorSystem.actorOf(YellowPagesActor.rootProps())
  }

  def createAll(): Unit = {
    val factory = new ConnectionFactory()
    val rabbitConnection = actorSystem.actorOf(ConnectionActor.props(factory), "rabbitmqtest")
    rootYellowPages ! RegistrationRequestMessage(rabbitConnection, "RabbitConnection", Topic() :+ Communication :+ RabbitMQ, ConnectionHandler)

    createCommunicationActors()
    createConnectionManagerActor()
    createDBMasterActors()
    createDBDefaultWorkers()
    createDBExtraWorkers()
    createAuthenticationActor()
    createYellowPagesActors()
  }

  def shutdown(): Unit = {
    actorSystem.terminate()
    Await.result(actorSystem.whenTerminated, timeout.duration)
  }

  private def createCommunicationActors() : Unit = {
    actorSystem.actorOf(RabbitMQChannelCreatorActor.channelCreatorProps(rootYellowPages, Topic() :+ Communication :+ RabbitMQ))
    actorSystem.actorOf(RabbitMQNamingActor.namingProps(rootYellowPages, Topic() :+ Communication :+ RabbitMQ))
    actorSystem.actorOf(RabbitMQPublisherActor.publisherProps(rootYellowPages, Topic() :+ Communication :+ RabbitMQ))
    actorSystem.actorOf(RabbitMQSubscriberActor.subscriberProps(rootYellowPages, Topic() :+ Communication :+ RabbitMQ))

    actorSystem.actorOf(RabbitMQUpdatesReceiverActor.updatesReceiverProps(rootYellowPages, Topic() :+ Communication :+ Updates :+ RabbitMQ))
    actorSystem.actorOf(RabbitMQNotificationsSenderActor.notificationsSenderProps(rootYellowPages, Topic() :+ Communication :+ Notifications :+ RabbitMQ))
    actorSystem.actorOf(RabbitMQCollaborationMembersActor.collaborationMemberProps(rootYellowPages, Topic() :+ Communication :+ Collaborations  :+ RabbitMQ))

    actorSystem.actorOf(NotificationsDispatcherActor.notificationsDispatcherProps(rootYellowPages, Topic() :+ Communication :+ Notifications))

    actorSystem.actorOf(FirebaseActor.firebaseProps(rootYellowPages, Topic() :+ Communication :+ Notifications :+ Firebase))
  }

  private def createConnectionManagerActor() : Unit = {
    actorSystem.actorOf(ConnectionManagerActor.connectionManagerProps(rootYellowPages, Topic() :+ Database))
  }

  private def createDBMasterActors() : Unit = {
    actorSystem.actorOf(DBMasterActor.dbMasterProps(rootYellowPages, Topic() :+ Database))
    actorSystem.actorOf(DBMasterNote.dbMasterNoteProps(rootYellowPages, Topic() :+ Database :+ Note))
    actorSystem.actorOf(DBMasterModule.dbMasterModuleProps(rootYellowPages, Topic() :+ Database :+ Module))
    actorSystem.actorOf(DBMasterCollaboration.dbMasterCollaborationProps(rootYellowPages, Topic() :+ Database :+ Collaboration))
    actorSystem.actorOf(DBMasterMember.dbMasterMemberProps(rootYellowPages, Topic() :+ Database :+ Member))
  }

  private def createDBDefaultWorkers() : Unit = {
    actorSystem.actorOf(DBWorkerNoteActor.dbWorkerNoteProps(rootYellowPages, Topic() :+ Database :+ Note))
    actorSystem.actorOf(DBWorkerModulesActor.dbWorkerModuleProps(rootYellowPages, Topic() :+ Database :+ Module))
    actorSystem.actorOf(DBWorkerCollaborationActor.dbWorkerCollaborationProps(rootYellowPages, Topic() :+ Database :+ Collaboration))
    actorSystem.actorOf(DBWorkerMemberActor.dbWorkerMemberProps(rootYellowPages, Topic() :+ Database :+ Member))
  }

  private def createDBExtraWorkers() : Unit = {
    actorSystem.actorOf(DBWorkerAuthenticationActor.dbWorkerAuthenticationProps(rootYellowPages, Topic() :+ Database))
    actorSystem.actorOf(DBWorkerChangeModuleStateActor.dbWorkerChangeModuleStateProps(rootYellowPages, Topic() :+ Database :+ Module))
    actorSystem.actorOf(DBWorkerCheckMemberExistenceActor.dbWorkerCheckMemberExistenceProps(rootYellowPages, Topic() :+ Database :+ Member))
    actorSystem.actorOf(DBWorkerGetCollaborationActor.dbWorkerGetCollaborationProps(rootYellowPages, Topic() :+ Database :+ Collaboration))
  }

  private def createAuthenticationActor() : Unit = {
    actorSystem.actorOf(AuthenticationActor.authenticationProps(rootYellowPages, Topic() :+ Authentication))
  }

  private def createYellowPagesActors() : Unit = {

    actorSystem.actorOf(PrinterActor.printerProps(rootYellowPages, Topic() :+ General))

    actorSystem.actorOf(YellowPagesActor.topicProps(rootYellowPages, Topic() :+ Communication))

    actorSystem.actorOf(YellowPagesActor.topicProps(rootYellowPages, Topic() :+ Database))

    actorSystem.actorOf(YellowPagesActor.topicProps(rootYellowPages, Topic() :+ Communication :+ RabbitMQ))

    actorSystem.actorOf(YellowPagesActor.topicProps(rootYellowPages, Topic() :+ Database :+ Note))
    actorSystem.actorOf(YellowPagesActor.topicProps(rootYellowPages, Topic() :+ Database :+ Module))
    actorSystem.actorOf(YellowPagesActor.topicProps(rootYellowPages, Topic() :+ Database :+ Collaboration))
    actorSystem.actorOf(YellowPagesActor.topicProps(rootYellowPages, Topic() :+ Database :+ Member))
  }
}