package org.gammf.collabora.util

/**
  * A very simple trait that represent an (incomplete, for now) update message
  */
trait UpdateMessage {
  def target: String
  def messageType: String
  def user: String
  def note: SimpleNote
}
