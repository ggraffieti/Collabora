package org.gammf.collabora.database.messages

import org.gammf.collabora.util.SimpleNote

trait QueryMessage

trait QueryNoteMessage extends QueryMessage {
  def note: SimpleNote
  def collaborationID: String
  def userID: String
}

case class InsertNoteMessage(note: SimpleNote, collaborationID: String, userID: String) extends QueryNoteMessage
case class UpdateNoteMessage(note: SimpleNote, collaborationID: String, userID: String) extends QueryNoteMessage
case class DeleteNoteMessage(note: SimpleNote, collaborationID: String, userID: String) extends QueryNoteMessage