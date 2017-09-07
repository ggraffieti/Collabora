package org.gammf.collabora.authentication.actors

import akka.actor.{Actor, ActorRef}
import akka.pattern.{ask, pipe}
import akka.util.Timeout
import org.gammf.collabora.authentication.messages._
import org.gammf.collabora.database.messages.{AuthenticationMessage, GetAllCollaborationsMessage}
import org.gammf.collabora.util.{Collaboration, CollaborationRight, CollaborationType, CollaborationUser, UpdateMessage, UpdateMessageTarget, UpdateMessageType}

import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

class AuthenticationActor(private val dbActor: ActorRef) extends Actor {

  private implicit val timeout: Timeout = Timeout(5 seconds)

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
