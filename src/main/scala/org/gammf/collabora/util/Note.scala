package org.gammf.collabora.util

trait Note {
  def id: Option[String]
  def content: String
  def expiration: Option[String]
  def location: Option[Double, Double]
  def previousNotes: Option[List[String]]
  def state: String
  def user: Option[String]

  override def toString: String =
    "{ Note -- id=" + id +
    ", content=" + content +
    ", expiration=" + expiration +
    ", location=" + location +
    ", previousNotes=" + previousNotes +
    ", state=" + state +
    ", user=" + user +
    " }"

}
