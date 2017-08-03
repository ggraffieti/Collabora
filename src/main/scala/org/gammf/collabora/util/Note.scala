package org.gammf.collabora.util

import java.util.Date

/**
  * Simple repreentation of a note
  * @param id the id of the note
  * @param content the content of the note
  * @param expiration the expiration date
  * @param location the location where the note
  * @param previousNotes previous associated notes (this note cannot be completed until all previous notes are
  *                      not completed.
  * @param state the state of the note (doing, done, todo...)
  */
class Note(val id: Option[String] = None, val content: String, val expiration: Option[Date] = None,
           val location: Option[(Double, Double)] = None, val previousNotes: Option[List[String]] = None,
           val state: String) {
}
