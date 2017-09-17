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

  import org.gammf.collabora.database._

  implicit object BSONtoNoteState extends BSONDocumentReader[NoteState] {
    def read(state: BSONDocument): NoteState =
      NoteState(
        definition = state.getAs[String](NOTE_STATE_DEFINITION).get,
        username = state.getAs[String](NOTE_STATE_RESPONSIBLE)
      )
  }

  implicit object NoteStatetoBSON extends BSONDocumentWriter[NoteState] {
    def write(noteState: NoteState): BSONDocument = {
      BSONDocument(
        NOTE_STATE_DEFINITION -> noteState.definition,
        { if (noteState.username.isDefined) NOTE_STATE_RESPONSIBLE -> noteState.username.get else BSONDocument()}
      )
    }
  }
}

object Note {
  def apply(id: Option[String] = None, content: String, expiration: Option[DateTime] = None, location: Option[Location] = None, previousNotes: Option[List[String]] = None, state: NoteState, module: Option[String] = None): Note = SimpleNote(id, content, expiration, location, previousNotes, state, module)

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

  import org.gammf.collabora.database._

  implicit object BSONtoNote extends BSONDocumentReader[Note] {
    def read(doc: BSONDocument): Note = {
      Note(
        id = doc.getAs[BSONObjectID](NOTE_ID).map(id => id.stringify),
        content = doc.getAs[String](NOTE_CONTENT).get,
        expiration = doc.getAs[DateTime](NOTE_EXPIRATION),
        location = doc.getAs[Location](NOTE_LOCATION),
        previousNotes = doc.getAs[List[BSONObjectID]](NOTE_PREVIOUS_NOTES).map(l => l.map(bsonID => bsonID.stringify)),
        state = doc.getAs[NoteState](NOTE_STATE).get,
        module = doc.getAs[BSONObjectID](NOTE_MODULE).map(m => m.stringify)
      )
    }
  }

  implicit object NotetoBSON extends BSONDocumentWriter[Note] {
    def write(note: Note): BSONDocument = {
      BSONDocument(
        NOTE_ID -> { if (note.id.isDefined) BSONObjectID.parse(note.id.get).get else BSONObjectID.generate() },
        NOTE_CONTENT -> note.content,
        NOTE_STATE -> note.state,
        { if (note.expiration.isDefined) NOTE_EXPIRATION -> note.expiration.get else BSONDocument() },
        { if (note.previousNotes.isDefined) NOTE_PREVIOUS_NOTES -> BSONArray(note.previousNotes.get.map(e => BSONObjectID.parse(e).get)) else BSONDocument()  },
        { if (note.location.isDefined) NOTE_LOCATION -> note.location.get else BSONDocument() },
        { if (note.module.isDefined) NOTE_MODULE -> BSONObjectID.parse(note.module.get).get else BSONDocument() }
      )
    }
  }
}

