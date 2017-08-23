package org.gammf.collabora.util

import org.joda.time.DateTime
import play.api.libs.json.{JsPath, Reads, Writes}
import play.api.libs.functional.syntax._
import reactivemongo.bson.{BSONArray, BSONDocument, BSONDocumentReader, BSONDocumentWriter, BSONObjectID}


/**
  * Represents a Note
  */
trait Note {
  def id: Option[String]
  def content: String
  def expiration: Option[DateTime]
  def location: Option[Location]
  def previousNotes: Option[List[String]]
  def state: NoteState
  def module: Option[String]

  override def toString: String =
    "{ Note -- id=" + id +
    ", content=" + content +
    ", expiration=" + expiration +
    ", location=" + location +
    ", previousNotes=" + previousNotes +
    ", state=" + state +
    ", module=" + module +
    " }"
}

case class NoteState(definition: String, username: Option[String] = None)

object NoteState {
  implicit val noteStateReads: Reads[NoteState] = (
    (JsPath \ "definition").read[String] and
      (JsPath \ "responsible").readNullable[String]
    )(NoteState.apply _)

  implicit val noteStateWrites: Writes[NoteState] = (
    (JsPath \ "definition").write[String] and
      (JsPath \ "responsible").writeNullable[String]
    )(unlift(NoteState.unapply))

  implicit object BSONtoNoteState extends BSONDocumentReader[NoteState] {
    def read(state: BSONDocument): NoteState =
      NoteState(
        definition = state.getAs[String]("definition").get,
        username = state.getAs[String]("responsible")
      )
  }
}

object Note {
  def apply(id: Option[String], content: String, expiration: Option[DateTime], location: Option[Location], previousNotes: Option[List[String]], state: NoteState, module: Option[String]): Note = SimpleNote(id, content, expiration, location, previousNotes, state, module)

  def unapply(arg: Note): Option[(Option[String], String, Option[DateTime], Option[Location], Option[List[String]], NoteState, Option[String])] = Some((arg.id, arg.content, arg.expiration, arg.location, arg.previousNotes, arg.state, arg.module))

  implicit val noteReads: Reads[Note] = (
    (JsPath \ "id").readNullable[String] and
      (JsPath \ "content").read[String] and
      (JsPath \ "expiration").readNullable[DateTime] and
      (JsPath \ "location").readNullable[Location] and
      (JsPath \ "previousNotes").readNullable[List[String]] and
      (JsPath \ "state").read[NoteState] and
      (JsPath \ "module").readNullable[String]
    )(Note.apply _)

  implicit val noteWrites: Writes[Note] = (
    (JsPath \ "id").writeNullable[String] and
      (JsPath \ "content").write[String] and
      (JsPath \ "expiration").writeNullable[DateTime] and
      (JsPath \ "location").writeNullable[Location] and
      (JsPath \ "previousNotes").writeNullable[List[String]] and
      (JsPath \ "state").write[NoteState] and
      (JsPath \ "module").writeNullable[String]
    )(unlift(Note.unapply))

  implicit object BSONtoNote extends BSONDocumentReader[Note] {
    def read(doc: BSONDocument): Note = {
      Note(
        id = doc.getAs[BSONObjectID]("id").map(id => id.stringify),
        content = doc.getAs[String]("content").get,
        expiration = doc.getAs[DateTime]("expiration"),
        location = doc.getAs[Location]("location"),
        previousNotes = doc.getAs[List[BSONObjectID]]("previousNotes").map(l => l.map(bsonID => bsonID.stringify)),
        state = doc.getAs[NoteState]("state").get,
        module = doc.getAs[BSONObjectID]("module").map(m => m.stringify)
      )
    }
  }

  implicit object NotetoBSON extends BSONDocumentWriter[Note] {
    def write(note: Note): BSONDocument = {
      var newNote = BSONDocument()
      if (note.id.isDefined) newNote = newNote.merge("id" -> BSONObjectID.parse(note.id.get).get)
      else newNote = newNote.merge("id" -> BSONObjectID.generate())
      newNote = newNote.merge(BSONDocument("content" -> note.content))
      if (note.state.username.isDefined) newNote = newNote.merge(BSONDocument("state" -> BSONDocument("definition" -> note.state.definition, "responsible" -> note.state.username.get)))
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
      if (note.module.isDefined) {
        newNote = newNote.merge("module" -> BSONObjectID.parse(note.module.get).get)
      }
      newNote
    }
  }
}

