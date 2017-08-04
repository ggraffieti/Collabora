package org.gammf.collabora.database

import akka.actor.Actor
import org.gammf.collabora.database.messages.{InsertNoteMessage, RequestAllNotesMessage}
import play.api.libs.json.JsObject
import reactivemongo.api.collections.bson.BSONCollection
import reactivemongo.api.{Cursor, FailoverStrategy, MongoConnection}
import reactivemongo.bson.{BSONArray, BSONDocument, BSONObjectID}
import reactivemongo.play.json.BSONFormats

import scala.concurrent.{Await, Future}
import scala.concurrent.duration._
import scala.util.{Failure, Success}
import scala.concurrent.ExecutionContext.Implicits.global

class DBActor(connection: MongoConnection) extends Actor {

  var notesCollection: BSONCollection = _

  override def preStart(): Unit = {
    def notesFut: Future[BSONCollection] = connection.database("collabora", FailoverStrategy())
      .map(_.collection("note", FailoverStrategy()))
    notesCollection = Await.result(notesFut, 10.seconds)

  }

  override def receive: Receive = {
    case m: RequestAllNotesMessage => {
      val res: Future[List[BSONDocument]] = notesCollection.find(BSONDocument()).cursor[BSONDocument]().collect[List](100, Cursor.FailOnError[List[BSONDocument]]())
      res onComplete {
        case Success(l) => l.foreach(e => println(BSONFormats.BSONDocumentFormat.writes(e).as[JsObject]))
        case Failure(e) => e.printStackTrace()
      }
    }
    case m: InsertNoteMessage => {
      val note = m.note
      var newNote = BSONDocument("content" -> note.content)
      if (note.user.isDefined) newNote = newNote.merge(BSONDocument("state" -> BSONDocument("definition" -> note.state, "username" -> note.user.get)))
      else newNote = newNote.merge(BSONDocument("state" -> BSONDocument("definition" -> note.state)))
      if (note.expiration.isDefined) newNote = newNote.merge(BSONDocument("expiration" -> note.expiration.get))
      if (note.previousNotes.isDefined) {
        val arr = BSONArray(note.previousNotes.get.map(e => BSONObjectID.parse(e).get))
        newNote = newNote.merge(BSONDocument("previousNotes" -> arr))
      }
      if (note.location.isDefined) {
        newNote = newNote.merge(BSONDocument("location" -> BSONDocument(
          "latitude" -> note.location.get._1,
          "longitude" -> note.location.get._2
        )))
      }

      val writeRes = notesCollection.insert(newNote)

      writeRes onComplete {
        case Success(res) => println("OKK  " + res)
        case Failure(e) => e.printStackTrace()
      }

    }
  }
}
