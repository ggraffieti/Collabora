package org.gammf.collabora.database.actors

import akka.actor.{ActorRef, Stash}
import org.gammf.collabora.database.messages._
import reactivemongo.bson.{BSON, BSONDocument, BSONObjectID}

import scala.util.{Failure, Success}

import scala.concurrent.ExecutionContext.Implicits.global

/**
  * A worker that performs query on modules.
  * @param connectionActor the actor that mantains the connection with the DB.
  */
class DBWorkerModulesActor(connectionActor: ActorRef) extends DBWorker(connectionActor) with Stash {

  override def receive: Receive = {
    case m: GetConnectionMessage =>
      connection = Some(m.connection)
      unstashAll()
    case _ if connection.isEmpty => stash()
    case message: InsertModuleMessage =>
      getCollaborationsCollection onComplete {
        case Success(collaborations) =>
          val bsonModule: BSONDocument = BSON.write(message.module)
          val selector = BSONDocument("_id" -> BSONObjectID.parse(message.collaborationID).get)
          collaborations.update(selector, BSONDocument("$push" -> BSONDocument("modules" -> bsonModule))) onComplete {
            case Success(_) => println("INSERTED MODULE")
            // val not = NotificationMessageImpl(messageType = "note_created", user = message.message.user, note = bsonNote.as[SimpleNote])
            // notificationActor ! PublishNotificationMessage(message.message.user, Json.toJson(not))
            case Failure(e) => e.printStackTrace() // TODO better error strategy
          }
        case Failure(e) => e.printStackTrace() // TODO better error strategy
      }
    case message: UpdateModuleMessage =>
      getCollaborationsCollection onComplete {
        case Success(collaborations) =>
          val bsonNote: BSONDocument = BSON.write(message.module)
          val selector = BSONDocument(
            "_id" -> BSONObjectID.parse(message.collaborationID).get,
            "modules.id" -> BSONObjectID.parse(message.module.id.get).get
          )
          collaborations.update(selector, BSONDocument("$set" -> BSONDocument("modules.$" -> bsonNote))) onComplete {
            case Success(_) => println("UPDATED MODULE")
            // val not = NotificationMessageImpl(messageType = "note_created", user = message.message.user, note = bsonNote.as[SimpleNote])
            // notificationActor ! PublishNotificationMessage(message.message.user, Json.toJson(not))
            case Failure(e) => e.printStackTrace() // TODO better error strategy
          }
        case Failure(e) => e.printStackTrace() // TODO better error strategy
      }
    case message: DeleteModuleMessage =>
      getCollaborationsCollection onComplete {
        case Success(collaborations) =>
          val bsonModule: BSONDocument = BSON.write(message.module)
          val selector = BSONDocument("_id" -> BSONObjectID.parse(message.collaborationID).get)
          collaborations.update(selector, BSONDocument("$pull" -> BSONDocument("modules" ->
            BSONDocument("id" -> BSONObjectID.parse(message.module.id.get).get)))) onComplete {
            case Success(_) => println("DELETED MODULE")
            // val not = NotificationMessageImpl(messageType = "note_created", user = message.message.user, note = bsonNote.as[SimpleNote])
            // notificationActor ! PublishNotificationMessage(message.message.user, Json.toJson(not))
            case Failure(e) => e.printStackTrace() // TODO better error strategy
          }
        case Failure(e) => e.printStackTrace() // TODO better error strategy
      }
  }

}
