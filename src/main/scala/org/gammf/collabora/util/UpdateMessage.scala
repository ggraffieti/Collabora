package org.gammf.collabora.util

trait UpdateMessage {
  def messageType: String
  def user: String
  def note: Note
}
