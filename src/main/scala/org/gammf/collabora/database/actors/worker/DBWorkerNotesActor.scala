package org.gammf.collabora.database.actors.worker

import akka.actor.{ActorRef, Stash}
import akka.pattern.pipe
import org.gammf.collabora.database._
import org.gammf.collabora.database.messages._
import org.gammf.collabora.util.Note
import reactivemongo.bson.{BSON, BSONDocument, BSONObjectID}

import scala.concurrent.ExecutionContext.Implicits.global

/**
  * A worker that performs query on notes.
  * @param connectionActor the actor that mantains the connection with the DB.
  */
class DBWorkerNotesActor(connectionActor: ActorRef) extends CollaborationsDBWorker(connectionActor) with Stash {

  override def receive: Receive = {

    case m: GetConnectionMessage =>
      connection = Some(m.connection)
      unstashAll()

    case _ if connection.isEmpty => stash()

    case message: InsertNoteMessage =>
      val bsonNote: BSONDocument = BSON.write(message.note) // necessary conversion, sets the noteID
      update(
        selector = BSONDocument(COLLABORATION_ID -> BSONObjectID.parse(message.collaborationID).get),
        query = BSONDocument("$push" -> BSONDocument(COLLABORATION_NOTES -> bsonNote)),
        okMessage = QueryOkMessage(InsertNoteMessage(bsonNote.as[Note], message.collaborationID, message.userID))
      ) pipeTo sender

    case message: UpdateNoteMessage =>
      update(
        selector = BSONDocument(
          COLLABORATION_ID -> BSONObjectID.parse(message.collaborationID).get,
          COLLABORATION_NOTES + "." + NOTE_ID -> BSONObjectID.parse(message.note.id.get).get
        ),
        query = BSONDocument("$set" -> BSONDocument(COLLABORATION_NOTES + ".$" -> message.note)),
        okMessage = QueryOkMessage(message)
      ) pipeTo sender

    case message: DeleteNoteMessage =>
      update(
        selector = BSONDocument(COLLABORATION_ID -> BSONObjectID.parse(message.collaborationID).get),
        query = BSONDocument("$pull" -> BSONDocument(COLLABORATION_NOTES ->
          BSONDocument(NOTE_ID -> BSONObjectID.parse(message.note.id.get).get))),
        okMessage = QueryOkMessage(message)
      ) pipeTo sender

  }
}