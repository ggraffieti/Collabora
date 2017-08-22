package org.gammf.collabora.util

import org.joda.time.DateTime

/**
  * Simple trait representing a user of Collabora
  */
trait User {
  def id:Option[String]
  def username: String
  def email: String
  def name: String
  def surname: String
  def birthday: DateTime

  override def toString: String = {
    "{ User -- id= " + id +
      ", username= " + username +
      ", email= " + email +
      ", name= " + name +
      ", surname= " + surname +
      ", birthday= " + birthday +
      " }"
  }
}
