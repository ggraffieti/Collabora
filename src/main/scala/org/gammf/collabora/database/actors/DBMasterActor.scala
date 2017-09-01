package org.gammf.collabora.database.actors

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import akka.pattern.{ask, pipe}
import akka.util.Timeout
import org.gammf.collabora.authentication.messages.LoginMessage
import org.gammf.collabora.communication.messages.{PublishMemberAddedMessage, PublishNotificationMessage}
import org.gammf.collabora.database.messages._
import org.gammf.collabora.util.UpdateMessageType.UpdateMessageType
import org.gammf.collabora.util.{CollaborationMessage, UpdateMessage, UpdateMessageTarget, UpdateMessageType}

import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global


/**
  * An actor that coordinate, create and act like a gateway for every request from and to the DB. It also create all the needed actors
  * @param system the actor system.
  */
class DBMasterActor(val system: ActorSystem, val notificationActor: ActorRef, val collaborationMemberActor: ActorRef) extends Actor {

  private var connectionManagerActor: ActorRef = _
  private var collaborationsActor: ActorRef = _
  private var getCollaborarionsActor: ActorRef = _
  private var modulesActor: ActorRef = _
  private var notesActor: ActorRef = _
  private var membersActor: ActorRef = _
  private var loginActor: ActorRef = _

  override def preStart(): Unit = {
    connectionManagerActor = system.actorOf(Props[ConnectionManagerActor])
    collaborationsActor = system.actorOf(Props.create(classOf[DBWorkerCollaborationsActor], connectionManagerActor))
    getCollaborarionsActor = system.actorOf(Props.create(classOf[DBWorkerGetCollaborationActor], connectionManagerActor, collaborationMemberActor))
    modulesActor = system.actorOf(Props.create(classOf[DBWorkerModulesActor], connectionManagerActor))
    notesActor = system.actorOf(Props.create(classOf[DBWorkerNotesActor], connectionManagerActor))
    membersActor = system.actorOf(Props.create(classOf[DBWorkerMemberActor], connectionManagerActor))
    loginActor = system.actorOf(Props.create(classOf[DBWorkerAuthentication], connectionManagerActor))
  }

  override def receive: Receive = {
    case message: UpdateMessage => message.target match {
      case UpdateMessageTarget.NOTE => message.messageType match {
        case UpdateMessageType.CREATION => notesActor ! InsertNoteMessage(message.note.get, message.collaborationId.get, message.user)
        case UpdateMessageType.UPDATING => notesActor ! UpdateNoteMessage(message.note.get, message.collaborationId.get, message.user)
        case UpdateMessageType.DELETION => notesActor ! DeleteNoteMessage(message.note.get, message.collaborationId.get, message.user)
      }
      case UpdateMessageTarget.COLLABORATION => message.messageType match {
        case UpdateMessageType.CREATION => collaborationsActor ! InsertCollaborationMessage(message.collaboration.get, message.user)
        case UpdateMessageType.UPDATING => collaborationsActor ! UpdateCollaborationMessage(message.collaboration.get, message.user)
        case UpdateMessageType.DELETION => collaborationsActor ! DeleteCollaborationMessage(message.collaboration.get, message.user)
      }
      case UpdateMessageTarget.MODULE => message.messageType match {
        case UpdateMessageType.CREATION => modulesActor ! InsertModuleMessage(message.module.get, message.collaborationId.get, message.user)
        case UpdateMessageType.UPDATING => modulesActor ! UpdateModuleMessage(message.module.get, message.collaborationId.get, message.user)
        case UpdateMessageType.DELETION => modulesActor ! DeleteModuleMessage(message.module.get, message.collaborationId.get, message.user)
      }
      case UpdateMessageTarget.MEMBER => message.messageType match {
        case UpdateMessageType.CREATION => membersActor ! InsertMemberMessage(message.member.get, message.collaborationId.get, message.user)
        case UpdateMessageType.UPDATING => membersActor ! UpdateMemberMessage(message.member.get, message.collaborationId.get, message.user)
        case UpdateMessageType.DELETION => membersActor ! DeleteMemberMessage(message.member.get, message.collaborationId.get, message.user)
      }
    }
    case QueryOkMessage(queryGoneWell) => queryGoneWell match {
      case query: QueryNoteMessage => notificationActor ! PublishNotificationMessage(query.collaborationID,UpdateMessage(target = UpdateMessageTarget.NOTE,
                                                                                                                         messageType = getUpdateTypeFromQueryMessage(query),
                                                                                                                         user = query.userID,
                                                                                                                         note = Some(query.note),
                                                                                                                         collaborationId = Some(query.collaborationID)))

      case query: QueryCollaborationMessage => query match {
        case _: InsertCollaborationMessage => collaborationMemberActor ! PublishMemberAddedMessage(query.userID, CollaborationMessage(user=query.userID,collaboration = query.collaboration))
        case _ => notificationActor ! PublishNotificationMessage(query.collaboration.id.get, UpdateMessage(target = UpdateMessageTarget.COLLABORATION,
                                                                                                            messageType = getUpdateTypeFromQueryMessage(query),
                                                                                                            user = query.userID,
                                                                                                            collaboration = Some(query.collaboration),
                                                                                                            collaborationId = Some(query.collaboration.id.get)))
      }
      case query: QueryModuleMessage => notificationActor ! PublishNotificationMessage(query.collaborationID, UpdateMessage(target = UpdateMessageTarget.MODULE,
                                                                                                                            messageType = getUpdateTypeFromQueryMessage(query),
                                                                                                                            user = query.userID,
                                                                                                                            module = Some(query.module),
                                                                                                                            collaborationId = Some(query.collaborationID)))

      case query: QueryMemberMessage => query match {
        case _: InsertMemberMessage => getCollaborarionsActor ! InsertMemberMessage(query.user, query.collaborationID, query.userID)
          notificationActor ! PublishNotificationMessage(query.collaborationID, UpdateMessage(target = UpdateMessageTarget.MEMBER,
                                                                                              messageType = getUpdateTypeFromQueryMessage(query),
                                                                                              user = query.userID,
                                                                                              member = Some(query.user),
                                                                                              collaborationId = Some(query.collaborationID)))

        case _ => notificationActor ! PublishNotificationMessage(query.collaborationID, UpdateMessage(target = UpdateMessageTarget.MEMBER,
                                                                                                      messageType = getUpdateTypeFromQueryMessage(query),
                                                                                                      user = query.userID,
                                                                                                      member = Some(query.user),
                                                                                                      collaborationId = Some(query.collaborationID)))
      }
    }

    case message: LoginMessage =>
      implicit val timeout: Timeout = Timeout(5 seconds)
      (loginActor ? message).mapTo[AuthenticationMessage] pipeTo sender

    case message: GetAllCollaborationsMessage => getCollaborarionsActor ! message

    case fail: QueryFailMessage => fail.error.printStackTrace() // TODO error handling
  }

  private def getUpdateTypeFromQueryMessage(query: QueryMessage): UpdateMessageType = query match {
    case _: InsertNoteMessage | _: InsertCollaborationMessage | _: InsertModuleMessage | _: InsertMemberMessage => UpdateMessageType.CREATION
    case _: UpdateNoteMessage | _: UpdateCollaborationMessage | _: UpdateModuleMessage | _: UpdateMemberMessage => UpdateMessageType.UPDATING
    case _: DeleteNoteMessage | _: DeleteCollaborationMessage | _: DeleteModuleMessage | _: DeleteMemberMessage => UpdateMessageType.DELETION
  }
}
