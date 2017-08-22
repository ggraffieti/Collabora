package org.gammf.collabora.database.actors

import akka.actor.{ActorRef, Stash}
import org.gammf.collabora.database.messages._
import reactivemongo.bson.{BSON, BSONDocument, BSONObjectID}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success}

/**
  * A worker that performs query on members.
  * @param connectionActor the actor that mantains the connection with the DB.
  */
class DBWorkerMemberActor(connectionActor: ActorRef) extends DBWorker(connectionActor) with Stash {

  override def receive: Receive = {
    case m: GetConnectionMessage =>
      connection = Some(m.connection)
      unstashAll()
    case _ if connection.isEmpty => stash()


    case message: InsertUserMessage =>
      val s = sender
      getCollaborationsCollection onComplete {
        case Success(collaborations) =>
          val bsonUser: BSONDocument = BSON.write(message.user)
          val selector = BSONDocument("_id" -> BSONObjectID.parse(message.collaborationID).get)
          collaborations.update(selector, BSONDocument("$push" -> BSONDocument("users" -> bsonUser))) onComplete {
            case Success(_) => s ! QueryOkMessage(message)
            case Failure(e) => e.printStackTrace() // TODO better error strategy
          }
        case Failure(e) => e.printStackTrace() // TODO better error strategy
      }
    case message: UpdateUserMessage =>
      val s = sender
      getCollaborationsCollection onComplete {
        case Success(collaborations) =>
          val bsonUser: BSONDocument = BSON.write(message.user)
          val selector = BSONDocument(
            "_id" -> BSONObjectID.parse(message.collaborationID).get,
            "users.user" -> message.user.user
          )
          collaborations.update(selector, BSONDocument("$set" -> BSONDocument("users.$" -> bsonUser))) onComplete {
            case Success(_) => s ! QueryOkMessage(message)
            case Failure(e) => e.printStackTrace() // TODO better error strategy
          }
        case Failure(e) => e.printStackTrace() // TODO better error strategy
      }
    case message: DeleteUserMessage =>
      val s = sender
      getCollaborationsCollection onComplete {
        case Success(collaborations) =>
          val bsonUser: BSONDocument = BSON.write(message.user)
          val selector = BSONDocument("_id" -> BSONObjectID.parse(message.collaborationID).get)
          collaborations.update(selector, BSONDocument("$pull" -> BSONDocument("users" ->
            BSONDocument("user" -> message.user.user)))) onComplete {
            case Success(_) => s ! QueryOkMessage(message)
            case Failure(e) => e.printStackTrace() // TODO better error strategy
          }
        case Failure(e) => e.printStackTrace() // TODO better error strategy
      }
  }

}
