package org.gammf.collabora.util

trait Module {

  def id: Option[String]
  def content: String
  def previousModules: Option[List[String]]
  def state: String


  override def toString: String = {
    "{ Module -- id=" + id +
      ", content=" + content +
      ", previousModules=" + previousModules +
      ", state=" + state
    " }"
  }
}
