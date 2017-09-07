package org.gammf.collabora.database.actors.master

import akka.actor.{ActorRef, ActorSystem, Props}
import org.gammf.collabora.communication.messages.PublishNotificationMessage
import org.gammf.collabora.database.actors.worker.DBWorkerModulesActor
import org.gammf.collabora.database.messages._
import org.gammf.collabora.util.{UpdateMessage, UpdateMessageTarget, UpdateMessageType}

class DBMasterModule(system: ActorSystem, connectionManagerActor: ActorRef, notificationActor: ActorRef) extends AbstractDBMaster {

  private[this] var moduleWorker: ActorRef = _

  override def preStart(): Unit = {
    moduleWorker = system.actorOf(Props.create(classOf[DBWorkerModulesActor], connectionManagerActor))
  }

  override def receive: Receive = {

    case message: UpdateMessage => message.target match {
      case UpdateMessageTarget.MODULE => message.messageType match {
        case UpdateMessageType.CREATION => moduleWorker ! InsertModuleMessage(message.module.get, message.collaborationId.get, message.user)
        case UpdateMessageType.UPDATING => moduleWorker ! UpdateModuleMessage(message.module.get, message.collaborationId.get, message.user)
        case UpdateMessageType.DELETION => moduleWorker ! DeleteModuleMessage(message.module.get, message.collaborationId.get, message.user)
      }
    }

    case QueryOkMessage(queryGoneWell) => queryGoneWell match {
      case query: QueryModuleMessage => notificationActor ! PublishNotificationMessage(query.collaborationID, UpdateMessage(
        target = UpdateMessageTarget.MODULE,
        messageType = getUpdateTypeFromQueryMessage(query),
        user = query.userID,
        module = Some(query.module),
        collaborationId = Some(query.collaborationID)))
      case _ => unhandled(_)
    }

    case fail: QueryFailMessage => fail.error.printStackTrace() // TODO error handling

    case _ => unhandled(_)
  }

}
