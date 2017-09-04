package org.gammf.collabora.database.actors

import akka.actor.{ActorRef, Stash}
import akka.pattern.pipe
import org.gammf.collabora.communication.messages.{PublishFirebaseNotification, PublishMemberAddedMessage, PublishUserLoginMessage}
import org.gammf.collabora.database.messages._
import org.gammf.collabora.util.{AllCollaborationsMessage, Collaboration, CollaborationMessage}
import org.gammf.collabora.database._
import reactivemongo.api.Cursor
import reactivemongo.bson.{BSONDocument, BSONObjectID}

import scala.util.{Failure, Success}
import scala.concurrent.ExecutionContext.Implicits.global

/***
  * A worker that perform a get on collaboration collection and communicate with CollaborationMembersActor
  * @param connectionActor the actor that mantains the connection with the DB.
  * @param collaborationActor the actor that manage the exchange "collaborations"
  */
class DBWorkerGetCollaborationActor(connectionActor: ActorRef, collaborationActor: ActorRef) extends CollaborationsDBWorker(connectionActor) with Stash {
  override def receive: Receive = {

    case m: GetConnectionMessage =>
      connection = Some(m.connection)
      unstashAll()

    case _ if connection.isEmpty => stash()

    case message: InsertMemberMessage =>
      getCollaborationsCollection onComplete {
        case Success(collaborations) =>
          val selector = BSONDocument(COLLABORATION_ID -> BSONObjectID.parse(message.collaborationID).get)
          collaborations.find(selector).one onComplete {
            case Success(s) => collaborationActor ! PublishMemberAddedMessage(message.user.user,CollaborationMessage(message.userID,s.get.as[Collaboration]))
            case Failure(e) => e.printStackTrace() // TODO better error strategy
          }
        case Failure(e) => e.printStackTrace() // TODO better error strategy
      }
    case message: GetCollaboration =>
      val s = sender
      getCollaborationsCollection onComplete {
        case Success(collaborations) =>
          val selector = BSONDocument(COLLABORATION_ID -> BSONObjectID.parse(message.collaborationID).get)
          collaborations.find(selector).one onComplete {
            case Success(c) => s ! PublishFirebaseNotification(message.collaborationID,c.get.as[Collaboration])
            case Failure(e) => e.printStackTrace() // TODO better error strategy
          }
        case Failure(e) => e.printStackTrace() // TODO better error strategy
      }

    case message: GetAllCollaborationsMessage =>
      getCollaborationsCollection.map(collaborations =>
        collaborations.find(BSONDocument(COLLABORATION_USERS ->
          BSONDocument("$elemMatch" -> BSONDocument(COLLABORATION_USER_USERNAME -> message.username)))
        ).cursor[BSONDocument]().collect[List](-1, Cursor.FailOnError[List[BSONDocument]]()))
        .flatten.map(list =>
        PublishUserLoginMessage(message.username,
          AllCollaborationsMessage(message.username, list.map(bson => bson.as[Collaboration])))) pipeTo collaborationActor

  }

}
