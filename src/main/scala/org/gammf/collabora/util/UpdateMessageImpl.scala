package org.gammf.collabora.util

import play.api.libs.json.{JsPath, Reads, Writes}
import play.api.libs.functional.syntax._

case class UpdateMessageImpl(messageType: String, user: String, note: SimpleNote) extends UpdateMessage {

}

object UpdateMessageImpl {
  implicit val updateMessageReads: Reads[UpdateMessageImpl] = (
    (JsPath \ "messageType").read[String] and
      (JsPath \ "user").read[String] and
      (JsPath \ "note").read[SimpleNote]
    )(UpdateMessageImpl.apply _)

  implicit val updateMessageWrites: Writes[UpdateMessageImpl] = (
    (JsPath \ "messageType").write[String] and
      (JsPath \ "user").write[String] and
      (JsPath \ "note").write[SimpleNote]
  ) (unlift(UpdateMessageImpl.unapply))
}
