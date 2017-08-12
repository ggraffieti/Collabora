package org.gammf.collabora.database.actors

import akka.actor.{ActorRef, Stash}
import org.gammf.collabora.database.messages._
import reactivemongo.bson.{BSON, BSONDocument, BSONObjectID}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success}

class DBWorkerNotesActor(connectionActor: ActorRef) extends DBWorker(connectionActor) with Stash {

  override def receive: Receive = {
    case m: GetConnectionMessage =>
      connection = Some(m.connection)
      unstashAll()
    case _ if connection.isEmpty => stash()
    case message: InsertNoteMessage =>
      getCollaborationsCollection onComplete {
        case Success(collaborations) =>
          val bsonNote: BSONDocument = BSON.write(message.note)
          val selector = BSONDocument("_id" -> BSONObjectID.parse(message.collaborationID).get)
          collaborations.update(selector, BSONDocument("$push" -> BSONDocument("notes" -> bsonNote))) onComplete {
          case Success(_) => println("INSERTED")
           // val not = NotificationMessageImpl(messageType = "note_created", user = message.message.user, note = bsonNote.as[SimpleNote])
           // notificationActor ! PublishNotificationMessage(message.message.user, Json.toJson(not))
          case Failure(e) => e.printStackTrace() // TODO better error strategy
        }
        case Failure(e) => e.printStackTrace() // TODO better error strategy
      }
    case message: UpdateNoteMessage =>
      getCollaborationsCollection onComplete {
        case Success(collaborations) =>
          val bsonNote: BSONDocument = BSON.write(message.note)
          val selector = BSONDocument(
            "_id" -> BSONObjectID.parse(message.collaborationID).get,
            "notes.id" -> BSONObjectID.parse(message.note.id.get).get
          )
          collaborations.update(selector, BSONDocument("$set" -> BSONDocument("notes.$" -> bsonNote))) onComplete {
            case Success(_) => println("UPDATED")
            // val not = NotificationMessageImpl(messageType = "note_created", user = message.message.user, note = bsonNote.as[SimpleNote])
            // notificationActor ! PublishNotificationMessage(message.message.user, Json.toJson(not))
            case Failure(e) => e.printStackTrace() // TODO better error strategy
          }
        case Failure(e) => e.printStackTrace() // TODO better error strategy
      }
    case message: DeleteNoteMessage =>
      getCollaborationsCollection onComplete {
        case Success(collaborations) =>
          val bsonNote: BSONDocument = BSON.write(message.note)
          val selector = BSONDocument("_id" -> BSONObjectID.parse(message.collaborationID).get)
          collaborations.update(selector, BSONDocument("$pull" -> BSONDocument("notes" ->
            BSONDocument("id" -> BSONObjectID.parse(message.note.id.get).get)))) onComplete {
            case Success(_) => println("DELETED")
            // val not = NotificationMessageImpl(messageType = "note_created", user = message.message.user, note = bsonNote.as[SimpleNote])
            // notificationActor ! PublishNotificationMessage(message.message.user, Json.toJson(not))
            case Failure(e) => e.printStackTrace() // TODO better error strategy
          }
        case Failure(e) => e.printStackTrace() // TODO better error strategy
      }
  }

}
