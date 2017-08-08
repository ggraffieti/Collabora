package org.gammf.collabora.util

import org.joda.time.DateTime

trait Note {
  def id: Option[String]
  def content: String
  def expiration: Option[DateTime]
  def location: Option[Location]
  def previousNotes: Option[List[String]]
  def state: NoteState

  override def toString: String =
    "{ Note -- id=" + id +
    ", content=" + content +
    ", expiration=" + expiration +
    ", location=" + location +
    ", previousNotes=" + previousNotes +
    ", state=" + state +
    " }"
}
