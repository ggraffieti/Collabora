package org.gammf.collabora.authentication.actors

import akka.actor.{Actor, ActorRef}
import org.gammf.collabora.authentication.messages._
import org.gammf.collabora.database.messages.GetAllCollaborationsMessage
import org.gammf.collabora.util.{Collaboration, CollaborationRight, CollaborationType, CollaborationUser, UpdateMessage, UpdateMessageTarget, UpdateMessageType}


/**
  * The authentication actor is bridge between the [[org.gammf.collabora.authentication.AuthenticationServer]] and the actor system.
  * Is the only actor that the server seen.
  * @param dbActor a reference to the [[org.gammf.collabora.database.actors.master.DBMasterActor]] of the system.
  */
class AuthenticationActor(private val dbActor: ActorRef) extends Actor {

  override def receive: Receive = {

    case message: LoginMessage => dbActor forward message
    case message: SigninMessage => dbActor forward message
    case message: SendAllCollaborationsMessage => dbActor ! GetAllCollaborationsMessage(message.username)
    case message: CreatePrivateCollaborationMessage =>
      dbActor ! UpdateMessage(
        target = UpdateMessageTarget.COLLABORATION,
        messageType = UpdateMessageType.CREATION,
        user = message.username,
        collaboration = Some(Collaboration(
          name = "private " + message.username,
          collaborationType = CollaborationType.PRIVATE,
          users = Some(List(CollaborationUser(
            user = message.username,
            right = CollaborationRight.ADMIN
          )))
        ))
      )
  }
}
