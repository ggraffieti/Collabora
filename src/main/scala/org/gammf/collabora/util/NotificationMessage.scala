package org.gammf.collabora.util

/**
  * Represents a message sent to the user to notify the creation/update/remotion of a note
  */
trait NotificationMessage {
  def messageType: String
  def user: String
  def note: Note
}
