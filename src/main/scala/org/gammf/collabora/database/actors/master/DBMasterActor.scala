package org.gammf.collabora.database.actors.master

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.pattern.{ask, pipe}
import akka.util.Timeout
import org.gammf.collabora.authentication.messages.{LoginMessage, SigninMessage, SigninResponseMessage}
import org.gammf.collabora.communication.messages.PublishErrorMessageInCollaborationExchange
import org.gammf.collabora.database.actors._
import org.gammf.collabora.database.actors.worker.{DBWorkerAuthenticationActor, DBWorkerChangeModuleStateActor, DBWorkerGetCollaborationActor}
import org.gammf.collabora.database.messages._
import org.gammf.collabora.util.{ServerErrorCode, ServerErrorMessage, UpdateMessage, UpdateMessageTarget}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

/**
  * The actor that coordinate, create and act like a gateway for every request from and to the DB. It also create all the needed actors.
  * @param system the actor system.
  * @param notificationActor the actor used for sending notification in the notification exchange
  * @param publishCollaborationExchangeActor the actor used for send notifications in the collaboration exchange
  */
class DBMasterActor(val system: ActorSystem, val notificationActor: ActorRef, val publishCollaborationExchangeActor: ActorRef) extends AbstractDBMaster {

  private[this] var connectionManagerActor: ActorRef = _
  private var noteManager: ActorRef = _
  private var collaborationManager: ActorRef = _
  private var moduleManager: ActorRef = _
  private var memberManager: ActorRef = _

  private var getCollaborarionsActor: ActorRef = _
  private var authenticationActor: ActorRef = _

  private var changeModuleStateActor: ActorRef = _

  implicit val timeout: Timeout = Timeout(5 seconds)

  override def preStart(): Unit = {
    connectionManagerActor = system.actorOf(Props[ConnectionManagerActor])

    changeModuleStateActor = system.actorOf(Props.create(classOf[DBWorkerChangeModuleStateActor], connectionManagerActor, self))

    noteManager = system.actorOf(Props.create(classOf[DBMasterNote], system, connectionManagerActor, notificationActor, changeModuleStateActor, publishCollaborationExchangeActor))
    collaborationManager = system.actorOf(Props.create(classOf[DBMasterCollaboration], system, connectionManagerActor, notificationActor, publishCollaborationExchangeActor))
    moduleManager = system.actorOf(Props.create(classOf[DBMasterModule], system, connectionManagerActor, notificationActor, publishCollaborationExchangeActor))

    getCollaborarionsActor = system.actorOf(Props.create(classOf[DBWorkerGetCollaborationActor], connectionManagerActor, publishCollaborationExchangeActor))
    memberManager = system.actorOf(Props.create(classOf[DBMasterMember], system, connectionManagerActor, notificationActor, getCollaborarionsActor, publishCollaborationExchangeActor))

    authenticationActor = system.actorOf(Props.create(classOf[DBWorkerAuthenticationActor], connectionManagerActor))
  }

  override def receive: Receive = {
    case message: UpdateMessage => message.target match {
      case UpdateMessageTarget.NOTE => noteManager forward message
      case UpdateMessageTarget.COLLABORATION => collaborationManager forward message
      case UpdateMessageTarget.MODULE => moduleManager forward message
      case UpdateMessageTarget.MEMBER => memberManager forward message
    }

    case message: LoginMessage => authenticationActor forward message

    case message: SigninMessage =>
      (authenticationActor ? message).mapTo[DBWorkerMessage].map(message =>
        SigninResponseMessage(message.isInstanceOf[QueryOkMessage])
      ) pipeTo sender

    case message: GetAllCollaborationsMessage => getCollaborarionsActor forward message

    case fail: QueryFailMessage => publishCollaborationExchangeActor ! PublishErrorMessageInCollaborationExchange(
      username = fail.username,
      message = ServerErrorMessage(user = fail.username, errorCode = ServerErrorCode.SERVER_ERROR)
    )
  }
}
