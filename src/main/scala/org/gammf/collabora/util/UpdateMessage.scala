package org.gammf.collabora.util

import org.gammf.collabora.util.UpdateMessageTarget.UpdateMessageTarget
import org.gammf.collabora.util.UpdateMessageType.UpdateMessageType
import play.api.libs.json.{JsString, Reads, Writes}

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
  def collaborationId: Option[String]
}


object UpdateMessageTarget extends Enumeration {
  type UpdateMessageTarget = Value
  val NOTE, MODULE, COLLABORATION, MEMBER = Value

  implicit val updateMessageTargetReads: Reads[UpdateMessageTarget] = js =>
    js.validate[String].map[UpdateMessageTarget](target =>
      UpdateMessageTarget.withName(target)
    )

  implicit val updateMessageTargetWrites: Writes[UpdateMessageTarget] = (target) =>  JsString(target.toString)
}

object UpdateMessageType extends Enumeration {
  type UpdateMessageType = Value
  val CREATION, UPDATING, DELETION = Value

  implicit val updateMessageTypeReads: Reads[UpdateMessageType] = js =>
    js.validate[String].map[UpdateMessageType](updateType =>
      UpdateMessageType.withName(updateType)
    )

  implicit val updateMessageTypeWrites: Writes[UpdateMessageTarget] = (updateType) =>  JsString(updateType.toString)
}