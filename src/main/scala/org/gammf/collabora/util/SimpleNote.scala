package org.gammf.collabora.util

import org.joda.time.DateTime

/**
  * Simple reprsentation of an immutable note
  * @param id the id of the note
  * @param content the content of the note
  * @param expiration the expiration date
  * @param location the location where the note
  * @param previousNotes previous associated notes (this note cannot be completed until all previous notes are
  *                      not completed.
  * @param state the state of the note
  */
case class SimpleNote(id: Option[String] = None, content: String, expiration: Option[DateTime] = None,
                 location: Option[Location] = None, previousNotes: Option[List[String]] = None,
                 state: NoteState, module: Option[String] = None) extends Note {
}