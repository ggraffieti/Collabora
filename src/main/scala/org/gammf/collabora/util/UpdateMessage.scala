package org.gammf.collabora.util

import org.gammf.collabora.util.UpdateMessageTarget.UpdateMessageTarget
import org.gammf.collabora.util.UpdateMessageType.UpdateMessageType
import play.api.libs.functional.syntax._
import play.api.libs.json.{JsPath, JsString, Reads, Writes}

/**
  * A very simple trait that represent an (incomplete, for now) update message
  */
trait UpdateMessage {
  def target: UpdateMessageTarget
  def messageType: UpdateMessageType
  def user: String
  def note: Option[Note]
  def module: Option[Module]
  def collaboration: Option[Collaboration]
  def member: Option[CollaborationUser]
  def collaborationId: Option[String]
}

object UpdateMessage {
  def apply(target: UpdateMessageTarget, messageType: UpdateMessageType, user: String, note: Option[Note] = None, module: Option[Module] = None, collaboration: Option[Collaboration] = None, member: Option[CollaborationUser] = None, collaborationId: Option[String] = None): UpdateMessage = UpdateMessageImpl(target, messageType, user, note, module, collaboration, member, collaborationId)

  def unapply(arg: UpdateMessage): Option[(UpdateMessageTarget, UpdateMessageType, String, Option[Note], Option[Module], Option[Collaboration], Option[CollaborationUser], Option[String])] = Some((arg.target, arg.messageType, arg.user, arg.note, arg.module, arg.collaboration, arg.member, arg.collaborationId))

  implicit val updateMessageReads: Reads[UpdateMessage] = (
    (JsPath \ "target").read[UpdateMessageTarget] and
      (JsPath \ "messageType").read[UpdateMessageType] and
      (JsPath \ "user").read[String] and
      (JsPath \ "note").readNullable[Note] and
      (JsPath \ "module").readNullable[Module] and
      (JsPath \ "collaboration").readNullable[Collaboration] and
      (JsPath \ "member").readNullable[CollaborationUser] and
      (JsPath \ "collaborationId").readNullable[String]
    )(UpdateMessage.apply _)

  implicit val updateMessageWrites: Writes[UpdateMessage] = (
    (JsPath \ "target").write[UpdateMessageTarget] and
      (JsPath \ "messageType").write[UpdateMessageType] and
      (JsPath \ "user").write[String] and
      (JsPath \ "note").writeNullable[Note] and
      (JsPath \ "module").writeNullable[Module] and
      (JsPath \ "collaboration").writeNullable[Collaboration] and
      (JsPath \ "member").writeNullable[CollaborationUser] and
      (JsPath \ "collaborationId").writeNullable[String]
    ) (unlift(UpdateMessage.unapply))
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