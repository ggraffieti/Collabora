package org.gammf.collabora.database.actors

import akka.actor.{ActorRef, Stash}
import akka.pattern.pipe
import org.gammf.collabora.database.messages._
import org.gammf.collabora.util.Note
import reactivemongo.bson.{BSON, BSONDocument, BSONObjectID}

import scala.concurrent.ExecutionContext.Implicits.global

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
      val bsonNote: BSONDocument = BSON.write(message.note) // necessary conversion, sets the noteID
      getCollaborationsCollection.map(collaborations => {
        collaborations.update(
          selector = BSONDocument("_id" -> BSONObjectID.parse(message.collaborationID).get),
          update = BSONDocument("$push" -> BSONDocument("notes" -> bsonNote))
        )
      }).map(_ => QueryOkMessage(InsertNoteMessage(bsonNote.as[Note], message.collaborationID, message.userID)))
        .recover({ case e: Exception =>  QueryFailMessage(e) }) pipeTo sender

    case message: UpdateNoteMessage =>
      getCollaborationsCollection.map(collaborations => {
        collaborations.update(
          selector = BSONDocument(
            "_id" -> BSONObjectID.parse(message.collaborationID).get,
            "notes.id" -> BSONObjectID.parse(message.note.id.get).get
          ),
          update = BSONDocument("$set" -> BSONDocument("notes.$" -> message.note))
        )
      }).map(_ => QueryOkMessage(message))
        .recover({ case e: Exception =>  QueryFailMessage(e) }) pipeTo sender

    case message: DeleteNoteMessage =>
      getCollaborationsCollection.map(collaborations => {
        collaborations.update(
          selector = BSONDocument("_id" -> BSONObjectID.parse(message.collaborationID).get),
          update = BSONDocument("$pull" -> BSONDocument("notes" ->
            BSONDocument("id" -> BSONObjectID.parse(message.note.id.get).get)))
        )
      }).map(_ => QueryOkMessage(message))
        .recover({ case e: Exception => QueryFailMessage(e) }) pipeTo sender

  }
}