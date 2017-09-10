package org.gammf.collabora.database.actors.master

import akka.actor.{ActorRef, ActorSystem, Props}
import org.gammf.collabora.communication.messages.{PublishErrorMessageInCollaborationExchange, PublishNotificationMessage}
import org.gammf.collabora.database.actors.worker.DBWorkerNotesActor
import org.gammf.collabora.database.messages._
import org.gammf.collabora.util.{ServerErrorCode, ServerErrorMessage, UpdateMessage, UpdateMessageTarget, UpdateMessageType}

/**
  * The master actor that manages all the query about notes.
  * @param system the actor system, used for create the needed workers.
  * @param connectionManagerActor The system-unique [[org.gammf.collabora.database.actors.ConnectionManagerActor]], used for mantain a
  *                               connection with the database
  * @param notificationActor The actor used for notify the client that a query is went good.
  * @param changeModuleStateActor the actor used for automatically change the module state on note insertion/deletion/updating
  * @param publishCollaborationExchangeActor the actor used for send notifications in the collaboration exchange
  */
class DBMasterNote(system: ActorSystem, connectionManagerActor: ActorRef, notificationActor: ActorRef, changeModuleStateActor: ActorRef, publishCollaborationExchangeActor: ActorRef) extends AbstractDBMaster {

  private[this] var noteWorker: ActorRef = _

  override def preStart(): Unit = {
    noteWorker = system.actorOf(Props.create(classOf[DBWorkerNotesActor], connectionManagerActor))
  }

  override def receive: Receive = {

    case message: UpdateMessage => message.target match {
      case UpdateMessageTarget.NOTE => message.messageType match {
        case UpdateMessageType.CREATION => noteWorker ! InsertNoteMessage(message.note.get, message.collaborationId.get, message.user)
        case UpdateMessageType.UPDATING => noteWorker ! UpdateNoteMessage(message.note.get, message.collaborationId.get, message.user)
        case UpdateMessageType.DELETION => noteWorker ! DeleteNoteMessage(message.note.get, message.collaborationId.get, message.user)
      }
    }

    case QueryOkMessage(queryGoneWell) => queryGoneWell match {
      case query: QueryNoteMessage => notificationActor ! PublishNotificationMessage(query.collaborationID, UpdateMessage(
        target = UpdateMessageTarget.NOTE,
        messageType = getUpdateTypeFromQueryMessage(query),
        user = query.userID,
        note = Some(query.note),
        collaborationId = Some(query.collaborationID)))
        if (query.note.module.isDefined) changeModuleStateActor ! ChangeModuleState(query.collaborationID, query.note.module.get)
      case _ => unhandled(_)
    }

    case fail: QueryFailMessage => publishCollaborationExchangeActor ! PublishErrorMessageInCollaborationExchange(
      username = fail.username,
      message = ServerErrorMessage(user = fail.username, errorCode = ServerErrorCode.SERVER_ERROR)
    )

    case _ => unhandled(_)
  }
}
