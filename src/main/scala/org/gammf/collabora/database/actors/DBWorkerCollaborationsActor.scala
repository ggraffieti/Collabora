package org.gammf.collabora.database.actors

import akka.actor.{ActorRef, Stash}
import akka.pattern.pipe
import org.gammf.collabora.database.messages._
import org.gammf.collabora.util.Collaboration
import reactivemongo.bson.{BSON, BSONDocument, BSONObjectID}

import scala.concurrent.ExecutionContext.Implicits.global

/**
  * A worker that performs query on collaborations.
  * @param connectionActor the actor that mantains the connection with the DB.
  */
class DBWorkerCollaborationsActor(connectionActor: ActorRef) extends CollaborationsDBWorker(connectionActor) with Stash {

  override def receive: Receive = {

    case m: GetConnectionMessage =>
      connection = Some(m.connection)
      unstashAll()

    case _ if connection.isEmpty => stash()

    case message: InsertCollaborationMessage =>
      val bsonCollaboration: BSONDocument = BSON.write(message.collaboration) // necessary conversion, sets the collaborationID
      insert(
        document = bsonCollaboration,
        okMessage = QueryOkMessage(InsertCollaborationMessage(bsonCollaboration.as[Collaboration], message.userID))
      ) pipeTo sender

    case message: UpdateCollaborationMessage =>
      update(
        selector = BSONDocument("_id" -> BSONObjectID.parse(message.collaboration.id.get).get),
        query = BSONDocument("$set" -> BSONDocument("name" -> message.collaboration.name)),
        okMessage = QueryOkMessage(message)
      ) pipeTo sender

    case message: DeleteCollaborationMessage =>
      delete(
        selector = BSONDocument("_id" -> BSONObjectID.parse(message.collaboration.id.get).get),
        okMessage = QueryOkMessage(message)
      ) pipeTo sender

  }
}