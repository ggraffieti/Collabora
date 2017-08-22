package org.gammf.collabora.database.actors

import akka.actor.{ActorRef, Stash}
import org.gammf.collabora.database.messages._
import org.gammf.collabora.util.Note
import reactivemongo.bson.{BSON, BSONDocument, BSONObjectID}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success}

/**
  * A worker that performs query on notes.
  * @param connectionActor the actor that mantains the connection with the DB.
  */
class DBWorkerNotesActor(connectionActor: ActorRef) extends DBWorker(connectionActor) with Stash {

  override def receive: Receive = {
    case m: GetConnectionMessage =>
      connection = Some(m.connection)
      unstashAll()
    case _ if connection.isEmpty => stash()
    case message: InsertNoteMessage =>
      val s = sender
      getCollaborationsCollection onComplete {
        case Success(collaborations) =>
          val bsonNote: BSONDocument = BSON.write(message.note)
          val selector = BSONDocument("_id" -> BSONObjectID.parse(message.collaborationID).get)
          collaborations.update(selector, BSONDocument("$push" -> BSONDocument("notes" -> bsonNote))) onComplete {
          case Success(_) => s ! QueryOkMessage(InsertNoteMessage(bsonNote.as[Note], message.collaborationID, message.userID))
          case Failure(e) => e.printStackTrace() // TODO better error strategy
        }
        case Failure(e) => e.printStackTrace() // TODO better error strategy
      }
    case message: UpdateNoteMessage =>
      val s = sender
      getCollaborationsCollection onComplete {
        case Success(collaborations) =>
          val bsonNote: BSONDocument = BSON.write(message.note)
          val selector = BSONDocument(
            "_id" -> BSONObjectID.parse(message.collaborationID).get,
            "notes.id" -> BSONObjectID.parse(message.note.id.get).get
          )
          collaborations.update(selector, BSONDocument("$set" -> BSONDocument("notes.$" -> bsonNote))) onComplete {
            case Success(_) => s ! QueryOkMessage(message)
            case Failure(e) => e.printStackTrace() // TODO better error strategy
          }
        case Failure(e) => e.printStackTrace() // TODO better error strategy
      }
    case message: DeleteNoteMessage =>
      val s = sender
      getCollaborationsCollection onComplete {
        case Success(collaborations) =>
          val bsonNote: BSONDocument = BSON.write(message.note)
          val selector = BSONDocument("_id" -> BSONObjectID.parse(message.collaborationID).get)
          collaborations.update(selector, BSONDocument("$pull" -> BSONDocument("notes" ->
            BSONDocument("id" -> BSONObjectID.parse(message.note.id.get).get)))) onComplete {
            case Success(_) => s ! QueryOkMessage(message)
            case Failure(e) => e.printStackTrace() // TODO better error strategy
          }
        case Failure(e) => e.printStackTrace() // TODO better error strategy
      }
  }

}
