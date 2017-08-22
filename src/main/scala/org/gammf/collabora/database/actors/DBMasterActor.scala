package org.gammf.collabora.database.actors

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import org.gammf.collabora.database.messages._
import org.gammf.collabora.util.{UpdateMessage, UpdateMessageTarget, UpdateMessageType}

/**
  * An actor that coordinate, create and act like a gateway for every request from and to the DB. It also create all the needed actors
  * @param system the actor system.
  */
class DBMasterActor(val system: ActorSystem) extends Actor {

  private var connectionManagerActor: ActorRef = _
  private var collaborationsActor: ActorRef = _
  private var modulesActor: ActorRef = _
  private var notesActor: ActorRef = _
  private var usersActor: ActorRef = _


  override def preStart(): Unit = {
    connectionManagerActor = system.actorOf(Props[ConnectionManagerActor])

    collaborationsActor = system.actorOf(Props.create(classOf[DBWorkerCollaborationsActor], connectionManagerActor))
    modulesActor = system.actorOf(Props.create(classOf[DBWorkerModulesActor], connectionManagerActor))
    notesActor = system.actorOf(Props.create(classOf[DBWorkerNotesActor], connectionManagerActor))
    usersActor = system.actorOf(Props.create(classOf[DBWorkerUsersActor], connectionManagerActor))
  }

  override def receive: Receive = {
    case message: UpdateMessage => message.target match {
      case UpdateMessageTarget.NOTE => message.messageType match {
        case UpdateMessageType.CREATION => notesActor ! InsertNoteMessage(message.note.get, message.collaboration.get.id.get, message.user)
        case UpdateMessageType.UPDATING => notesActor ! UpdateNoteMessage(message.note.get, message.collaboration.get.id.get, message.user)
        case UpdateMessageType.DELETION => notesActor ! DeleteNoteMessage(message.note.get, message.collaboration.get.id.get, message.user)
      }
      case UpdateMessageTarget.COLLABORATION => message.messageType match {
        case UpdateMessageType.CREATION => collaborationsActor ! InsertCollaborationMessage(message.collaboration.get, message.user)
        case UpdateMessageType.UPDATING => collaborationsActor ! UpdateCollaborationMessage(message.collaboration.get, message.user)
        case UpdateMessageType.DELETION => collaborationsActor ! DeleteCollaborationMessage(message.collaboration.get, message.user)
      }
      case UpdateMessageTarget.MODULE => message.messageType match {
        case UpdateMessageType.CREATION => modulesActor ! InsertModuleMessage(message.module.get, message.collaboration.get.id.get, message.user)
        case UpdateMessageType.UPDATING => modulesActor ! UpdateModuleMessage(message.module.get, message.collaboration.get.id.get, message.user)
        case UpdateMessageType.DELETION => modulesActor ! DeleteModuleMessage(message.module.get, message.collaboration.get.id.get, message.user)
      }
      case UpdateMessageTarget.MEMBER => message.messageType match {
        case UpdateMessageType.CREATION => usersActor ! InsertUserMessage(message.member.get, message.collaboration.get.id.get, message.user)
        case UpdateMessageType.UPDATING => usersActor ! UpdateUserMessage(message.member.get, message.collaboration.get.id.get, message.user)
        case UpdateMessageType.DELETION => usersActor ! DeleteUserMessage(message.member.get, message.collaboration.get.id.get, message.user)
      }
    }
  }
}
