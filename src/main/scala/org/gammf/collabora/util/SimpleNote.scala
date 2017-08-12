package org.gammf.collabora.util

import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import play.api.libs.json._
import play.api.libs.functional.syntax._
import reactivemongo.bson.{BSONArray, BSONDateTime, BSONDocument, BSONDocumentReader, BSONDocumentWriter, BSONHandler, BSONObjectID}

/**
  * Simple repreentation of an immutable note
  * @param id the id of the note
  * @param content the content of the note
  * @param expiration the expiration date
  * @param location the location where the note
  * @param previousNotes previous associated notes (this note cannot be completed until all previous notes are
  *                      not completed.
  * @param state the state of the note (doing, done, todo...)
  */
case class SimpleNote(id: Option[String] = None, content: String, expiration: Option[DateTime] = None,
                 location: Option[Location] = None, previousNotes: Option[List[String]] = None,
                 state: NoteState, module: Option[String] = None) extends Note {
}

case class Location(latitude: Double, longitude: Double)

case class NoteState(definition: String, username: Option[String])

object SimpleNote {
  implicit val locationReads: Reads[Location] = (
    (JsPath \ "latitude").read[Double] and
      (JsPath \ "longitude").read[Double]
  )(Location.apply _)

  implicit val noteStateReads: Reads[NoteState] = (
    (JsPath \ "definition").read[String] and
      (JsPath \ "username").readNullable[String]
  )(NoteState.apply _)

  private val jodaDateReads = Reads[DateTime](js =>
    js.validate[String].map[DateTime](dtString =>
      DateTime.parse(dtString, DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ"))
    )
  )
  implicit val dateReads: Reads[DateTime] = jodaDateReads

  implicit val noteReads: Reads[SimpleNote] = (
    (JsPath \ "id").readNullable[String] and
      (JsPath \ "content").read[String] and
      (JsPath \ "expiration").readNullable[DateTime] and
      (JsPath \ "location").readNullable[Location] and
      (JsPath \ "previousNotes").readNullable[List[String]] and
      (JsPath \ "state").read[NoteState] and
      (JsPath \ "module").readNullable[String]
  )(SimpleNote.apply _)



  implicit val locationWrites: Writes[Location] = (
    (JsPath \ "latitude").write[Double] and
      (JsPath \ "longitude").write[Double]
    )(unlift(Location.unapply))

  implicit val noteStateWrites: Writes[NoteState] = (
    (JsPath \ "definition").write[String] and
      (JsPath \ "username").writeNullable[String]
    )(unlift(NoteState.unapply))

  private val jodaDateWrites: Writes[DateTime] = (date) => JsString(date.toString())

  implicit val dateWrites: Writes[DateTime] = jodaDateWrites

  implicit val noteWrites: Writes[SimpleNote] = (
    (JsPath \ "id").writeNullable[String] and
      (JsPath \ "content").write[String] and
      (JsPath \ "expiration").writeNullable[DateTime] and
      (JsPath \ "location").writeNullable[Location] and
      (JsPath \ "previousNotes").writeNullable[List[String]] and
      (JsPath \ "state").write[NoteState] and
      (JsPath \ "module").writeNullable[String]
    )(unlift(SimpleNote.unapply))


  // BSON conversion

  implicit object BSONDateTimeHandler extends BSONHandler[BSONDateTime, DateTime] {
    def read(time: BSONDateTime) = new DateTime(time.value)
    def write(jdtime: DateTime) = BSONDateTime(jdtime.getMillis)
  }

  implicit object BSONtoLocation extends BSONDocumentReader[Location] {
    def read(doc: BSONDocument): Location =
      Location(
        latitude = doc.getAs[Double]("latitude").get,
        longitude = doc.getAs[Double]("longitude").get
      )
  }

  implicit object BSONtoNoteState extends BSONDocumentReader[NoteState] {
    def read(state: BSONDocument): NoteState =
      NoteState(
        definition = state.getAs[String]("definition").get,
        username = state.getAs[String]("username")
      )
  }

  implicit object BSONtoNote extends BSONDocumentReader[SimpleNote] {
    def read(doc: BSONDocument): SimpleNote = {
      SimpleNote(
        id = doc.getAs[BSONObjectID]("_id").map(id => id.stringify),
        content = doc.getAs[String]("content").get,
        expiration = doc.getAs[DateTime]("expiration"),
        location = doc.getAs[Location]("location"),
        previousNotes = doc.getAs[List[BSONObjectID]]("previousNotes").map(l => l.map(bsonID => bsonID.stringify)),
        state = doc.getAs[NoteState]("state").get,
        module = doc.getAs[BSONObjectID]("module").map(m => m.stringify)
      )
    }
  }

  implicit object NotetoBSON extends BSONDocumentWriter[SimpleNote] {
    def write(note: SimpleNote): BSONDocument = {
      var newNote = BSONDocument()
      if (note.id.isDefined) newNote = newNote.merge("_id" -> BSONObjectID.parse(note.id.get).get)
      else newNote = newNote.merge("_id" -> BSONObjectID.generate())
      newNote = newNote.merge(BSONDocument("content" -> note.content))
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
      if (note.module.isDefined) {
        newNote = newNote.merge("module" -> BSONObjectID.parse(note.module.get).get)
      }
      newNote
    }
  }
}