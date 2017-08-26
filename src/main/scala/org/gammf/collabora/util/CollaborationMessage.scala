package org.gammf.collabora.util

import play.api.libs.functional.syntax._
import play.api.libs.json.{JsPath, Reads, Writes}

/**
  * A very simple trait that represent a collaboration message
  */
trait CollaborationMessage {
  def user: String
  def collaboration: Collaboration
}

object CollaborationMessage {
  def apply(user: String, collaboration: Collaboration): CollaborationMessage = CollaborationMessageImpl(user,collaboration)

  def unapply(arg: CollaborationMessage): Option[(String,Collaboration)] = Some((arg.user,arg.collaboration))

  implicit val collaborationMessageReads: Reads[CollaborationMessage] = (
      (JsPath \ "user").read[String] and
      (JsPath \ "collaboration").read[Collaboration]
    )(CollaborationMessage.apply _)

  implicit val collaborationMessageWrites: Writes[CollaborationMessage] = (
      (JsPath \ "user").write[String] and
      (JsPath \ "collaboration").write[Collaboration]
    ) (unlift(CollaborationMessage.unapply))
}
