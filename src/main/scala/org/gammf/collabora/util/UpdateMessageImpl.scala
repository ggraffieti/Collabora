package org.gammf.collabora.util

import play.api.libs.functional.syntax.unlift
import play.api.libs.json.{JsPath, Reads, Writes}
import play.api.libs.functional.syntax._

/**
  * Simple for a update message
  * @param target the target of the message (note, module, collaboration, member)
  * @param messageType the type of the message (creation, update, deletion)
  * @param user the user that creates the update
  * @param note the note affected
  */
case class UpdateMessageImpl(target: String, messageType: String, user: String, note: SimpleNote) extends UpdateMessage {

}
object UpdateMessageImpl {
  implicit val updateMessageReads: Reads[UpdateMessageImpl] = (
    (JsPath \ "target").read[String] and
    (JsPath \ "messageType").read[String] and
      (JsPath \ "user").read[String] and
      (JsPath \ "note").read[SimpleNote]
    )(UpdateMessageImpl.apply _)

  implicit val updateMessageWrites: Writes[UpdateMessageImpl] = (
    (JsPath \ "target").write[String] and
    (JsPath \ "messageType").write[String] and
      (JsPath \ "user").write[String] and
      (JsPath \ "note").write[SimpleNote]
    ) (unlift(UpdateMessageImpl.unapply))
}