package org.gammf.collabora.util

import play.api.libs.json.{JsPath, Reads, Writes}
import play.api.libs.functional.syntax._

case class NotificationMessageImpl(messageType: String, user: String, note: SimpleNote) extends NotificationMessage {

}

object NotificationMessageImpl {
  implicit val updateMessageReads: Reads[NotificationMessageImpl] = (
    (JsPath \ "messageType").read[String] and
      (JsPath \ "user").read[String] and
      (JsPath \ "note").read[SimpleNote]
    )(NotificationMessageImpl.apply _)

  implicit val updateMessageWrites: Writes[NotificationMessageImpl] = (
    (JsPath \ "messageType").write[String] and
      (JsPath \ "user").write[String] and
      (JsPath \ "note").write[SimpleNote]
  ) (unlift(NotificationMessageImpl.unapply))
}
