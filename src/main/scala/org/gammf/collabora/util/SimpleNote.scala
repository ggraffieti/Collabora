package org.gammf.collabora.util

import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import play.api.libs.json.{JsPath, Reads}
import play.api.libs.functional.syntax._

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
                 state: NoteState) extends Note {
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
      (JsPath \ "state").read[NoteState]
  )(SimpleNote.apply _)
}