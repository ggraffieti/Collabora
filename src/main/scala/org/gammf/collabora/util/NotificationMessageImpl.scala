package org.gammf.collabora.util

import play.api.libs.json.{JsPath, Reads, Writes}
import play.api.libs.functional.syntax._

/**
  * Simple for a notification message
  * @param messageType the type of the message (creation, update, remotion)
  * @param user  the user that has done the update
  * @param note the note
  */
case class NotificationMessageImpl(messageType: String, user: String, note: SimpleNote) extends NotificationMessage {

}

object NotificationMessageImpl {
  implicit val notificationMessageReads: Reads[NotificationMessageImpl] = (
    (JsPath \ "messageType").read[String] and
      (JsPath \ "user").read[String] and
      (JsPath \ "note").read[SimpleNote]
    )(NotificationMessageImpl.apply _)

  implicit val notificationMessageWrites: Writes[NotificationMessageImpl] = (
    (JsPath \ "messageType").write[String] and
      (JsPath \ "user").write[String] and
      (JsPath \ "note").write[SimpleNote]
  ) (unlift(NotificationMessageImpl.unapply))
}
