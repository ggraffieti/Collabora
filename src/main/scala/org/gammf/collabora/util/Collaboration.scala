package org.gammf.collabora.util

import org.gammf.collabora.util.CollaborationRight.CollaborationRight
import org.gammf.collabora.util.CollaborationType.CollaborationType

trait Collaboration {

  def id: Option[String]
  def name: String
  def collaborationType: CollaborationType
  def users: Option[List[CollaborationUser]]
  def modules: Option[List[SimpleModule]]
  def notes: Option[List[SimpleNote]]

}

case class CollaborationUser(user: String, right: CollaborationRight)

object CollaborationType extends Enumeration {
  type CollaborationType = Value
  val PRIVATE, GROUP, PROJECT = Value
}

object CollaborationRight extends Enumeration {
  type CollaborationRight = Value
  val READ, WRITE, ADMIN = Value
}



