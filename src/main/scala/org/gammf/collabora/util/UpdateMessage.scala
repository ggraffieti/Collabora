package org.gammf.collabora.util

import org.gammf.collabora.util.UpdateMessageTarget.UpdateMessageTarget
import org.gammf.collabora.util.UpdateMessageType.UpdateMessageType

/**
  * A very simple trait that represent an (incomplete, for now) update message
  */
trait UpdateMessage {
  def target: UpdateMessageTarget
  def messageType: UpdateMessageType
  def user: String
  def note: Option[SimpleNote]
  def module: Option[SimpleModule]
  def collaboration: Option[SimpleCollaboration]
  def member: Option[CollaborationUser]
}


object UpdateMessageTarget extends Enumeration {
  type UpdateMessageTarget = Value
  val NOTE, MODULE, COLLABORATION, MEMBER = Value
}

object UpdateMessageType extends Enumeration {
  type UpdateMessageType = Value
  val CREATION, UPDATING, DELETION = Value
}