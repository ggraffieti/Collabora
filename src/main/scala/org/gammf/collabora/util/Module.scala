package org.gammf.collabora.util

/**
  * Represents a module: a group of notes in a project.
  */
trait Module {

  def id: Option[String]
  def description: String
  def previousModules: Option[List[String]]
  def state: String


  override def toString: String = {
    "{ Module -- id=" + id +
      ", content=" + description +
      ", previousModules=" + previousModules +
      ", state=" + state +
    " }"
  }
}
