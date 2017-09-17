package org.gammf.collabora.authentication.actors

import akka.actor.{ActorRef, Props}
import akka.pattern.{ask, pipe}
import akka.util.Timeout
import org.gammf.collabora.authentication.messages._
import org.gammf.collabora.database.messages._
import org.gammf.collabora.util.{Collaboration, CollaborationRight, CollaborationType, CollaborationUser, UpdateMessage, UpdateMessageTarget, UpdateMessageType}
import org.gammf.collabora.yellowpages.ActorService.ActorService
import org.gammf.collabora.yellowpages.actors.BasicActor
import org.gammf.collabora.yellowpages.util.Topic.ActorTopic
import org.gammf.collabora.yellowpages.util.Topic
import org.gammf.collabora.yellowpages.TopicElement._
import org.gammf.collabora.yellowpages.ActorService._

import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.language.postfixOps


/**
  * The authentication actor is bridge between the [[org.gammf.collabora.authentication.AuthenticationServer]] and the actor system.
  * Is the only actor that the server seen.
  */
class AuthenticationActor(override val yellowPages: ActorRef,
                          override val name: String,
                          override val topic: ActorTopic,
                          override val service: ActorService = Bridging) extends BasicActor {

  implicit val timeout: Timeout = Timeout(5 seconds)

  override def receive: Receive = {
    case message @ (_: LoginMessage | _: SigninMessage) => getActorOrElse(Topic() :+ Database, Master, message).foreach(_ forward message)
    case message: SendAllCollaborationsMessage => getActorOrElse(Topic() :+ Database, Master, message).foreach(_ forward GetAllCollaborationsMessage(message.username))
    case message: CreatePrivateCollaborationMessage =>
      getActorOrElse(Topic() :+ Database, Master, message).foreach(dbActor =>
      (dbActor ? buildInsertPrivateCollaborationMessage(buildPrivateCollaboration(message.username),
        message.username)).mapTo[DBWorkerMessage].map {
        case query: QueryOkMessage => query.queryGoneWell match {
          case q: InsertCollaborationMessage => Some(q.collaboration)
          case _ => None
        }
        case _ => None
      } pipeTo sender)
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

object AuthenticationActor {

  /**
    * Factory methods that return a [[Props]] to create a authentication registered actor
    * @param yellowPages the reference to the yellow pages root actor.
    * @param topic the topic to which this actor is going to be registered.
    * @return the [[Props]] to use to create a authentication actor.
    */
  def authenticationProps(yellowPages: ActorRef, topic: ActorTopic, name: String = "AuthenticationActor") : Props =
    Props(new AuthenticationActor(yellowPages = yellowPages, name = name, topic = topic))
}
