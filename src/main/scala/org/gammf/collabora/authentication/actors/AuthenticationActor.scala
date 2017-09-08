package org.gammf.collabora.authentication.actors

import akka.actor.{Actor, ActorRef}
import akka.pattern.{ask, pipe}
import akka.util.Timeout
import org.gammf.collabora.authentication.messages._
import org.gammf.collabora.database.messages._
import org.gammf.collabora.util.{Collaboration, CollaborationRight, CollaborationType, CollaborationUser, UpdateMessage, UpdateMessageTarget, UpdateMessageType}
import scala.concurrent.duration._

import scala.concurrent.ExecutionContext.Implicits.global

/**
  * The authentication actor is bridge between the [[org.gammf.collabora.authentication.AuthenticationServer]] and the actor system.
  * Is the only actor that the server seen.
  * @param dbActor a reference to the [[org.gammf.collabora.database.actors.master.DBMasterActor]] of the system.
  */
class AuthenticationActor(private val dbActor: ActorRef) extends Actor {

  implicit val timeout: Timeout = Timeout(5 seconds)

  override def receive: Receive = {

    case message: LoginMessage => dbActor forward message
    case message: SigninMessage => dbActor forward message
    case message: SendAllCollaborationsMessage => dbActor forward GetAllCollaborationsMessage(message.username)
    case message: CreatePrivateCollaborationMessage =>
      (dbActor ? buildInsertPrivateCollaborationMessage(buildPrivateCollaboration(message.username),
        message.username)).mapTo[DBWorkerMessage].map {
        case _: QueryOkMessage => Some(buildPrivateCollaboration(message.username))
        case _ => None
      } pipeTo sender
  }

  private def buildPrivateCollaboration(username: String): Collaboration =
    Collaboration(
      name = "private " + username,
      collaborationType = CollaborationType.PRIVATE,
      users = Some(List(CollaborationUser(
        user = username,
        right = CollaborationRight.ADMIN
      ))))

  private def buildInsertPrivateCollaborationMessage(collaboration: Collaboration, username: String): UpdateMessage =
    UpdateMessage(
      target = UpdateMessageTarget.COLLABORATION,
      messageType = UpdateMessageType.CREATION,
      user = username,
      collaboration = Some(collaboration)
    )

}
