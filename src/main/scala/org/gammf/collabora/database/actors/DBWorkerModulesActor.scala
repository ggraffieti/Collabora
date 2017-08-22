package org.gammf.collabora.database.actors

import akka.actor.{ActorRef, Stash}
import org.gammf.collabora.database.messages._
import org.gammf.collabora.util.Module
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
      val s = sender
      getCollaborationsCollection onComplete {
        case Success(collaborations) =>
          val bsonModule: BSONDocument = BSON.write(message.module)
          val selector = BSONDocument("_id" -> BSONObjectID.parse(message.collaborationID).get)
          collaborations.update(selector, BSONDocument("$push" -> BSONDocument("modules" -> bsonModule))) onComplete {
            case Success(_) => s ! QueryOkMessage(InsertModuleMessage(bsonModule.as[Module],message.collaborationID, message.userID))
            case Failure(e) => e.printStackTrace() // TODO better error strategy
          }
        case Failure(e) => e.printStackTrace() // TODO better error strategy
      }
    case message: UpdateModuleMessage =>
      val s = sender
      getCollaborationsCollection onComplete {
        case Success(collaborations) =>
          val bsonNote: BSONDocument = BSON.write(message.module)
          val selector = BSONDocument(
            "_id" -> BSONObjectID.parse(message.collaborationID).get,
            "modules.id" -> BSONObjectID.parse(message.module.id.get).get
          )
          collaborations.update(selector, BSONDocument("$set" -> BSONDocument("modules.$" -> bsonNote))) onComplete {
            case Success(_) => s ! QueryOkMessage(message)
            case Failure(e) => e.printStackTrace() // TODO better error strategy
          }
        case Failure(e) => e.printStackTrace() // TODO better error strategy
      }
    case message: DeleteModuleMessage =>
      val s = sender
      getCollaborationsCollection onComplete {
        case Success(collaborations) =>
          val bsonModule: BSONDocument = BSON.write(message.module)
          val selector = BSONDocument("_id" -> BSONObjectID.parse(message.collaborationID).get)
          collaborations.update(selector, BSONDocument("$pull" -> BSONDocument("modules" ->
            BSONDocument("id" -> BSONObjectID.parse(message.module.id.get).get)))) onComplete {
            case Success(_) => s ! QueryOkMessage(message)
            case Failure(e) => e.printStackTrace() // TODO better error strategy
          }
        case Failure(e) => e.printStackTrace() // TODO better error strategy
      }
  }

}
