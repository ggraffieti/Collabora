package org.gammf.collabora.database.actors

import akka.actor.{ActorRef, Stash}
import org.gammf.collabora.database.messages._
import org.gammf.collabora.util.Collaboration
import reactivemongo.bson.{BSON, BSONDocument, BSONObjectID}

import scala.util.{Failure, Success}
import scala.concurrent.ExecutionContext.Implicits.global

/**
  * A worker that performs query on collaborations.
  * @param connectionActor the actor that mantains the connection with the DB.
  */
class DBWorkerCollaborationsActor(connectionActor: ActorRef) extends DBWorker(connectionActor) with Stash {

  override def receive: Receive = {
    case m: GetConnectionMessage =>
      connection = Some(m.connection)
      unstashAll()
    case _ if connection.isEmpty => stash()
    case message: InsertCollaborationMessage =>
      val s = sender
      getCollaborationsCollection onComplete {
        case Success(collaborations) =>
          val bsonCollaboration: BSONDocument = BSON.write(message.collaboration)
          collaborations.insert(bsonCollaboration) onComplete {
            case Success(_) => s ! QueryOkMessage(InsertCollaborationMessage(bsonCollaboration.as[Collaboration], message.userID))
            case Failure(e) => e.printStackTrace() // TODO better error strategy
          }
        case Failure(e) => e.printStackTrace() // TODO better error strategy
      }
    case message: UpdateCollaborationMessage =>
      val s = sender
      getCollaborationsCollection onComplete {
        case Success(collaborations) =>
          val bsonCollaboration: BSONDocument = BSON.write(message.collaboration)
          val selector = BSONDocument("_id" -> BSONObjectID.parse(message.collaboration.id.get).get)
          collaborations.update(selector, BSONDocument("$set" ->
            BSONDocument("name" -> bsonCollaboration.get("name"))
          )) onComplete {
            case Success(_) => s ! QueryOkMessage(message)
            case Failure(e) => e.printStackTrace() // TODO better error strategy
          }
        case Failure(e) => e.printStackTrace() // TODO better error strategy
      }
    case message: DeleteCollaborationMessage =>
      val s = sender
      getCollaborationsCollection onComplete {
        case Success(collaborations) =>
          val bsonCollaboration: BSONDocument = BSON.write(message.collaboration)
          val selector = BSONDocument("_id" -> BSONObjectID.parse(message.collaboration.id.get).get)
          collaborations.remove(selector) onComplete {
            case Success(_) => s ! QueryOkMessage(message)
            case Failure(e) => e.printStackTrace() // TODO better error strategy
          }
        case Failure(e) => e.printStackTrace() // TODO better error strategy
      }
  }

}
