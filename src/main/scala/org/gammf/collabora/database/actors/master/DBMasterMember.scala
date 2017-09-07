package org.gammf.collabora.database.actors.master

import akka.actor.{ActorRef, ActorSystem, Props}
import org.gammf.collabora.communication.messages.PublishNotificationMessage
import org.gammf.collabora.database.actors.worker.DBWorkerMemberActor
import org.gammf.collabora.database.messages._
import org.gammf.collabora.util.{UpdateMessage, UpdateMessageTarget, UpdateMessageType}

class DBMasterMember(system: ActorSystem, connectionManagerActor: ActorRef, notificationActor: ActorRef, getCollaborationActor: ActorRef) extends AbstractDBMaster {

  private[this] var memberWorker: ActorRef = _

  override def preStart(): Unit = {
    memberWorker = system.actorOf(Props.create(classOf[DBWorkerMemberActor], connectionManagerActor))
  }

  override def receive: Receive = {

    case message: UpdateMessage => message.target match {
      case UpdateMessageTarget.MEMBER => message.messageType match {
        case UpdateMessageType.CREATION => memberWorker ! InsertMemberMessage(message.member.get, message.collaborationId.get, message.user)
        case UpdateMessageType.UPDATING => memberWorker ! UpdateMemberMessage(message.member.get, message.collaborationId.get, message.user)
        case UpdateMessageType.DELETION => memberWorker ! DeleteMemberMessage(message.member.get, message.collaborationId.get, message.user)
      }
    }

    case QueryOkMessage(queryGoneWell) => queryGoneWell match {
      case query: QueryMemberMessage => query match {
        case _: InsertMemberMessage => getCollaborationActor ! InsertMemberMessage(query.user, query.collaborationID, query.userID)
          notificationActor ! PublishNotificationMessage(query.collaborationID, UpdateMessage(
            target = UpdateMessageTarget.MEMBER,
            messageType = getUpdateTypeFromQueryMessage(query),
            user = query.userID,
            member = Some(query.user),
            collaborationId = Some(query.collaborationID)))

        case _ => notificationActor ! PublishNotificationMessage(query.collaborationID, UpdateMessage(
          target = UpdateMessageTarget.MEMBER,
          messageType = getUpdateTypeFromQueryMessage(query),
          user = query.userID,
          member = Some(query.user),
          collaborationId = Some(query.collaborationID)))
      }

      case _ => unhandled(_)
    }

    case fail: QueryFailMessage => fail.error.printStackTrace() // TODO error handling

    case _ => unhandled(_)
  }
}
