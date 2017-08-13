package org.gammf.collabora.database.actors

import akka.actor.{ActorRef, Stash}
import org.gammf.collabora.database.messages._
import reactivemongo.bson.{BSON, BSONDocument, BSONObjectID}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success}

class DBWorkerUsersActor(connectionActor: ActorRef) extends DBWorker(connectionActor) with Stash {

  override def receive: Receive = {
    case m: GetConnectionMessage =>
      connection = Some(m.connection)
      unstashAll()
    case _ if connection.isEmpty => stash()


    case message: InsertUserMessage =>
      getCollaborationsCollection onComplete {
        case Success(collaborations) =>
          val bsonUser: BSONDocument = BSON.write(message.user)
          val selector = BSONDocument("_id" -> BSONObjectID.parse(message.collaborationID).get)
          collaborations.update(selector, BSONDocument("$push" -> BSONDocument("users" -> bsonUser))) onComplete {
            case Success(_) => println("INSERTED USER")
            // val not = NotificationMessageImpl(messageType = "note_created", user = message.message.user, note = bsonNote.as[SimpleNote])
            // notificationActor ! PublishNotificationMessage(message.message.user, Json.toJson(not))
            case Failure(e) => e.printStackTrace() // TODO better error strategy
          }
        case Failure(e) => e.printStackTrace() // TODO better error strategy
      }
    case message: UpdateUserMessage =>
      getCollaborationsCollection onComplete {
        case Success(collaborations) =>
          val bsonUser: BSONDocument = BSON.write(message.user)
          val selector = BSONDocument(
            "_id" -> BSONObjectID.parse(message.collaborationID).get,
            "users.user" -> message.user.user
          )
          collaborations.update(selector, BSONDocument("$set" -> BSONDocument("users.$" -> bsonUser))) onComplete {
            case Success(_) => println("UPDATED USER")
            // val not = NotificationMessageImpl(messageType = "note_created", user = message.message.user, note = bsonNote.as[SimpleNote])
            // notificationActor ! PublishNotificationMessage(message.message.user, Json.toJson(not))
            case Failure(e) => e.printStackTrace() // TODO better error strategy
          }
        case Failure(e) => e.printStackTrace() // TODO better error strategy
      }
    case message: DeleteUserMessage =>
      getCollaborationsCollection onComplete {
        case Success(collaborations) =>
          val bsonUser: BSONDocument = BSON.write(message.user)
          val selector = BSONDocument("_id" -> BSONObjectID.parse(message.collaborationID).get)
          collaborations.update(selector, BSONDocument("$pull" -> BSONDocument("users" ->
            BSONDocument("user" -> message.user.user)))) onComplete {
            case Success(_) => println("DELETED USER")
            // val not = NotificationMessageImpl(messageType = "note_created", user = message.message.user, note = bsonNote.as[SimpleNote])
            // notificationActor ! PublishNotificationMessage(message.message.user, Json.toJson(not))
            case Failure(e) => e.printStackTrace() // TODO better error strategy
          }
        case Failure(e) => e.printStackTrace() // TODO better error strategy
      }
  }

}
