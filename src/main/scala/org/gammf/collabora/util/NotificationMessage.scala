package org.gammf.collabora.util

trait NotificationMessage {
  def messageType: String
  def user: String
  def note: Note
}
