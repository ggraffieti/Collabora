package org.gammf.collabora.database.actors

import akka.actor.{Actor, ActorRef, Stash}
import org.gammf.collabora.database.messages._
import org.gammf.collabora.util.SimpleNote
import play.api.libs.json.JsObject
import reactivemongo.api.collections.bson.BSONCollection
import reactivemongo.api.{Cursor, FailoverStrategy, MongoConnection}
import reactivemongo.bson.{BSONArray, BSONDocument, BSONObjectID}
import reactivemongo.play.json.BSONFormats

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Success}

class DBActor(connectionActor: ActorRef, printActor: ActorRef) extends Actor with Stash {

  var connection: Option[MongoConnection] = None

  override def preStart(): Unit = connectionActor ! new AskConnectionMessage()


  override def receive: Receive = {
    case m: GetConnectionMessage =>
      connection = Some(m.connection)
      unstashAll()
    case _ if connection.isEmpty => stash()
    case _: RequestAllNotesMessage =>
      getNotesCollection onComplete {
        case Success(notesCollection) => notesCollection.find(BSONDocument())
          .cursor[SimpleNote]().collect[List](100, Cursor.FailOnError[List[SimpleNote]]()) onComplete {
          case Success(list) => printActor ! new PrintMessage(list.toString)
          case Failure(e) => e.printStackTrace() // TODO improve error strategy
        }
        case Failure(e) => e.printStackTrace() // TODO improve error strategy
      }
    case m: InsertNoteMessage =>
      // TODO automathic conversion Note <-> BSONDOocument
      val note = m.note
      var newNote = BSONDocument("content" -> note.content)
      if (note.state.username.isDefined) newNote = newNote.merge(BSONDocument("state" -> BSONDocument("definition" -> note.state.definition, "username" -> note.state.username.get)))
      else newNote = newNote.merge(BSONDocument("state" -> BSONDocument("definition" -> note.state.definition)))
      if (note.expiration.isDefined) newNote = newNote.merge(BSONDocument("expiration" -> note.expiration.get.toDate))
      if (note.previousNotes.isDefined) {
        val arr = BSONArray(note.previousNotes.get.map(e => BSONObjectID.parse(e).get))
        newNote = newNote.merge(BSONDocument("previousNotes" -> arr))
      }
      if (note.location.isDefined) {
        newNote = newNote.merge(BSONDocument("location" -> BSONDocument(
          "latitude" -> note.location.get.latitude,
          "longitude" -> note.location.get.longitude
        )))
      }

      getNotesCollection onComplete {
        case Success(notesCollection) => notesCollection.insert(newNote) onComplete {
          case Success(res) => printActor ! new PrintMessage("OKK " + res)
          case Failure(e) => e.printStackTrace() // TODO better error strategy
        }
        case Failure(e) => e.printStackTrace() // TODO better error strategy
      }
  }

  private def getNotesCollection: Future[BSONCollection] = {
    if (connection.isDefined)
      connection.get.database("collabora", FailoverStrategy())
        .map(_.collection("note", FailoverStrategy()))
    else
      throw new Error() // TODO more specific error
  }
}
