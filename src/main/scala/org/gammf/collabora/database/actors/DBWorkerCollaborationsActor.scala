package org.gammf.collabora.database.actors

import akka.actor.{ActorRef, Stash}
import org.gammf.collabora.database.messages._
import reactivemongo.bson.{BSON, BSONDocument, BSONObjectID}

import scala.util.{Failure, Success}
import scala.concurrent.ExecutionContext.Implicits.global

class DBWorkerCollaborationsActor(connectionActor: ActorRef) extends DBWorker(connectionActor) with Stash {

  override def receive: Receive = {
    case m: GetConnectionMessage =>
      connection = Some(m.connection)
      unstashAll()
    case _ if connection.isEmpty => stash()
    case message: InsertCollaborationMessage =>
      getCollaborationsCollection onComplete {
        case Success(collaborations) =>
          val bsonCollaboration: BSONDocument = BSON.write(message.collaboration)
          collaborations.insert(bsonCollaboration) onComplete {
            case Success(_) => println("INSERTED COLLABORATION")
            // val not = NotificationMessageImpl(messageType = "note_created", user = message.message.user, note = bsonNote.as[SimpleNote])
            // notificationActor ! PublishNotificationMessage(message.message.user, Json.toJson(not))
            case Failure(e) => e.printStackTrace() // TODO better error strategy
          }
        case Failure(e) => e.printStackTrace() // TODO better error strategy
      }
    case message: UpdateCollaborationMessage =>
      getCollaborationsCollection onComplete {
        case Success(collaborations) =>
          val bsonCollaboration: BSONDocument = BSON.write(message.collaboration)
          val selector = BSONDocument("_id" -> BSONObjectID.parse(message.collaboration.id.get).get)
          collaborations.update(selector, BSONDocument("$set" ->
            BSONDocument(
              "name" -> bsonCollaboration.get("name"),
              "type" -> bsonCollaboration.get("type"),
              "users" -> bsonCollaboration.get("users")
            )
          )) onComplete {
            case Success(_) => println("UPDATED COLLABORATION")
            // val not = NotificationMessageImpl(messageType = "note_created", user = message.message.user, note = bsonNote.as[SimpleNote])
            // notificationActor ! PublishNotificationMessage(message.message.user, Json.toJson(not))
            case Failure(e) => e.printStackTrace() // TODO better error strategy
          }
        case Failure(e) => e.printStackTrace() // TODO better error strategy
      }
    case message: DeleteCollaborationMessage =>
      getCollaborationsCollection onComplete {
        case Success(collaborations) =>
          val bsonCollaboration: BSONDocument = BSON.write(message.collaboration)
          val selector = BSONDocument("_id" -> BSONObjectID.parse(message.collaboration.id.get).get)
          collaborations.remove(selector) onComplete {
            case Success(_) => println("DELETED COLLABORATION")
            // val not = NotificationMessageImpl(messageType = "note_created", user = message.message.user, note = bsonNote.as[SimpleNote])
            // notificationActor ! PublishNotificationMessage(message.message.user, Json.toJson(not))
            case Failure(e) => e.printStackTrace() // TODO better error strategy
          }
        case Failure(e) => e.printStackTrace() // TODO better error strategy
      }
  }

}
