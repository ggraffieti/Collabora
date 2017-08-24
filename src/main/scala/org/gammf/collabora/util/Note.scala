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

  implicit object NoteStatetoBSON extends BSONDocumentWriter[NoteState] {
    def write(noteState: NoteState): BSONDocument = {
      BSONDocument(
        "definition" -> noteState.definition,
        { if (noteState.username.isDefined) "responsible" -> noteState.username.get else BSONDocument()}
      )
    }
  }

  implicit object NotetoBSON extends BSONDocumentWriter[Note] {
    def write(note: Note): BSONDocument = {
      BSONDocument(
        "id" -> { if (note.id.isDefined) BSONObjectID.parse(note.id.get).get else BSONObjectID.generate() },
        "content" -> note.content,
        "state" -> note.state,
        { if (note.expiration.isDefined) "expiration" -> note.expiration.get.toDate else BSONDocument() },
        { if (note.previousNotes.isDefined) "previousNotes" -> BSONArray(note.previousNotes.get.map(e => BSONObjectID.parse(e).get)) else BSONDocument()  },
        { if (note.location.isDefined) "location" -> note.location.get else BSONDocument() },
        { if (note.module.isDefined) "module" -> BSONObjectID.parse(note.module.get).get else BSONDocument() }
      )
    }
  }
}

