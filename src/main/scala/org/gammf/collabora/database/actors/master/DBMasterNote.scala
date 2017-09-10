package org.gammf.collabora.database.actors.master

import akka.actor.{ActorRef, ActorSystem, Props}
import org.gammf.collabora.communication.messages.PublishNotificationMessage
import org.gammf.collabora.database.actors.worker.DBWorkerNotesActor
import org.gammf.collabora.database.messages._
import org.gammf.collabora.util.{UpdateMessage, UpdateMessageTarget, UpdateMessageType}

/**
  * The master actor that manages all the query about notes.
  * @param system the actor system, used for create the needed workers.
  * @param connectionManagerActor The system-unique [[org.gammf.collabora.database.actors.ConnectionManagerActor]], used for mantain a
  *                               connection with the database
  * @param notificationActor The actor used for notify the client that a query is went good.
  */
class DBMasterNote(system: ActorSystem, connectionManagerActor: ActorRef, notificationActor: ActorRef, changeModuleStateActor: ActorRef) extends AbstractDBMaster {

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

    case fail: QueryFailMessage => fail.error.printStackTrace() // TODO error handling

    case _ => unhandled(_)
  }
}
