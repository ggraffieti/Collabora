package org.gammf.collabora.database.actors.master

import akka.actor.{ActorRef, ActorSystem, Props}
import org.gammf.collabora.communication.messages.{PublishCollaborationInCollaborationExchange, PublishErrorMessageInCollaborationExchange, PublishNotificationMessage}
import org.gammf.collabora.database.actors.worker.DBWorkerCollaborationsActor
import org.gammf.collabora.database.messages._
import org.gammf.collabora.util.{CollaborationMessage, CollaborationType, ServerErrorCode, ServerErrorMessage, UpdateMessage, UpdateMessageTarget, UpdateMessageType}

/**
  * The master actor that manages all the query about collaborations.
  * @param system the actor system, used for create the needed workers.
  * @param connectionManagerActor The system-unique [[org.gammf.collabora.database.actors.ConnectionManagerActor]], used for mantain a
  *                               connection with the database
  * @param notificationActor The actor used for notify the client that a query is went good.
  * @param publishCollaborationExchangeActor the actor used for send notifications in the collaboration exchange.
  */
class DBMasterCollaboration(system: ActorSystem, connectionManagerActor: ActorRef, notificationActor: ActorRef, publishCollaborationExchangeActor: ActorRef) extends AbstractDBMaster {

  private[this] var collaborationWorker: ActorRef = _

  override def preStart(): Unit = {
    collaborationWorker = system.actorOf(Props.create(classOf[DBWorkerCollaborationsActor], connectionManagerActor))
  }

  override def receive: Receive = {

    case message: UpdateMessage => message.target match {
      case UpdateMessageTarget.COLLABORATION => message.messageType match {
        case UpdateMessageType.CREATION =>
          if (message.collaboration.get.collaborationType == CollaborationType.PRIVATE)
            collaborationWorker forward InsertCollaborationMessage(message.collaboration.get, message.user)
          else
            collaborationWorker ! InsertCollaborationMessage(message.collaboration.get, message.user)
        case UpdateMessageType.UPDATING => collaborationWorker ! UpdateCollaborationMessage(message.collaboration.get, message.user)
        case UpdateMessageType.DELETION => collaborationWorker ! DeleteCollaborationMessage(message.collaboration.get, message.user)
      }
    }

    case QueryOkMessage(queryGoneWell) => queryGoneWell match {
      case query: QueryCollaborationMessage => query match {
        case _: InsertCollaborationMessage => publishCollaborationExchangeActor ! PublishCollaborationInCollaborationExchange(query.userID, CollaborationMessage(user=query.userID,collaboration = query.collaboration))
        case _ => notificationActor ! PublishNotificationMessage(query.collaboration.id.get, UpdateMessage(
          target = UpdateMessageTarget.COLLABORATION,
          messageType = getUpdateTypeFromQueryMessage(query),
          user = query.userID,
          collaboration = Some(query.collaboration),
          collaborationId = Some(query.collaboration.id.get)))
      }
      case _ => unhandled(_)
    }

    case fail: QueryFailMessage => publishCollaborationExchangeActor ! PublishErrorMessageInCollaborationExchange(
      username = fail.username,
      message = ServerErrorMessage(user = fail.username, errorCode = ServerErrorCode.SERVER_ERROR)
    )

    case _ => unhandled(_)

  }
}
