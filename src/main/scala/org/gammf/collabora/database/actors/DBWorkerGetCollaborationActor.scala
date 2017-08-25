package org.gammf.collabora.database.actors

import akka.actor.{ActorRef, Stash}
import org.gammf.collabora.communication.messages.PublishMemberAddedMessage
import org.gammf.collabora.database.messages._
import org.gammf.collabora.util.{Collaboration, CollaborationMessage}
import play.api.libs.json.Json
import reactivemongo.bson.{BSONDocument, BSONObjectID}

import scala.util.{Failure, Success}
import scala.concurrent.ExecutionContext.Implicits.global

class DBWorkerGetCollaborationActor(connectionActor: ActorRef, collaborationActor: ActorRef) extends DBWorker(connectionActor) with Stash {
  override def receive: Receive = {
    case m: GetConnectionMessage =>
      connection = Some(m.connection)
      unstashAll()
    case _ if connection.isEmpty => stash()
    case message: InsertUserMessage =>
      getCollaborationsCollection onComplete {
        case Success(collaborations) =>
          val selector = BSONDocument("_id" -> BSONObjectID.parse(message.collaborationID).get)
          collaborations.find(selector).one onComplete {
            case Success(s) => println(s.get.as[Collaboration]) ; collaborationActor ! PublishMemberAddedMessage(message.user.user,CollaborationMessage(message.userID,s.get.as[Collaboration]))
            case Failure(e) => e.printStackTrace() // TODO better error strategy
          }
        case Failure(e) => e.printStackTrace() // TODO better error strategy
      }

  }

}
