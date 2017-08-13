package org.gammf.collabora.util

trait Module {

  def id: Option[String]
  def description: String
  def previousModules: Option[List[String]]
  def state: String


  override def toString: String = {
    "{ Module -- id=" + id +
      ", content=" + description +
      ", previousModules=" + previousModules +
      ", state=" + state
    " }"
  }
}
