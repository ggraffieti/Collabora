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
class DBWorkerCollaborationsActor(connectionActor: ActorRef) extends DBWorker(connectionActor) with Stash {

  override def receive: Receive = {

    case m: GetConnectionMessage =>
      connection = Some(m.connection)
      unstashAll()

    case _ if connection.isEmpty => stash()

    case message: InsertCollaborationMessage =>
      val bsonCollaboration: BSONDocument = BSON.write(message.collaboration) // necessary conversion, sets the collaborationID
      getCollaborationsCollection.map(collaborations =>
        collaborations.insert(bsonCollaboration)
      ).map(_ => QueryOkMessage(InsertCollaborationMessage(bsonCollaboration.as[Collaboration], message.userID)))
        .recover({ case e: Exception => QueryFailMessage(e) }) pipeTo sender

    case message: UpdateCollaborationMessage =>
      getCollaborationsCollection.map(collaborations =>
        collaborations.update(
          selector = BSONDocument("_id" -> BSONObjectID.parse(message.collaboration.id.get).get),
          update = BSONDocument("$set" -> BSONDocument("name" -> message.collaboration.name))
        )
      ).map(_ => QueryOkMessage(message))
        .recover({ case e: Exception => QueryFailMessage(e) }) pipeTo sender

    case message: DeleteCollaborationMessage =>
      getCollaborationsCollection.map(collaborations =>
        collaborations.remove(BSONDocument("_id" -> BSONObjectID.parse(message.collaboration.id.get).get))
      ).map(_ => QueryOkMessage(message))
        .recover({ case e: Exception => QueryFailMessage(e) }) pipeTo sender

  }
}
