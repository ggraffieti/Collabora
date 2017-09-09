package org.gammf.collabora.util

import org.gammf.collabora.util.ErrorCode.ErrorCode
import play.api.libs.json.{JsPath, JsString, Writes}
import play.api.libs.functional.syntax._

/**
  * A [[CollaborationExchangeMessage]] that represents an error occured in the server. This error
  * has to be delivered at the user that had caused it, via the collaboration exchange.
  */
trait ServerErrorMessage extends CollaborationExchangeMessage {
  def collaborationId: String
  def errorCode: ErrorCode
}

object ErrorCode extends Enumeration {
  type ErrorCode = Value
  val SERVER_ERROR,
      MEMBER_NOT_FOUND = Value

  implicit val errorCodeWrites: Writes[ErrorCode] = (errorCode) => JsString(errorCode.toString)
}

object ServerErrorMessage {

  def apply(user: String, collaborationId: String, errorCode: ErrorCode) =
    BasicServerErrorMessage(user, collaborationId, errorCode)

  def unapply(arg: ServerErrorMessage): Option[(String, String, ErrorCode)] = Some((arg.user, arg.collaborationId, arg.errorCode))

  implicit val errorMessageWrites: Writes[ServerErrorMessage] = (
    (JsPath \ "user").write[String] and
      (JsPath \ "collaborationId").write[String] and
      (JsPath \ "errorCode").write[ErrorCode]
  )(unlift(ServerErrorMessage.unapply))

}

case class BasicServerErrorMessage(user: String, collaborationId: String, errorCode: ErrorCode) extends ServerErrorMessage