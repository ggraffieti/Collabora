package org.gammf.collabora.util

import org.gammf.collabora.util.ServerErrorCode.ServerErrorCode
import play.api.libs.json.{JsPath, JsString, Writes}
import play.api.libs.functional.syntax._

/**
  * A message that represents an error occured in the server. This error
  * has to be delivered at the user that had caused it, via the collaboration exchange.
  */
trait ServerErrorMessage {
  def user: String
  def errorCode: ServerErrorCode
}

object ServerErrorCode extends Enumeration {
  type ServerErrorCode = Value
  val SERVER_ERROR,
      MEMBER_NOT_FOUND = Value

  implicit val errorCodeWrites: Writes[ServerErrorCode] = (errorCode) => JsString(errorCode.toString)
}

object ServerErrorMessage {

  def apply(user: String, errorCode: ServerErrorCode) =
    BasicServerErrorMessage(user, errorCode)

  def unapply(arg: ServerErrorMessage): Option[(String, ServerErrorCode)] = Some((arg.user, arg.errorCode))

  implicit val errorMessageWrites: Writes[ServerErrorMessage] = (
    (JsPath \ "user").write[String] and
      (JsPath \ "errorCode").write[ServerErrorCode]
  )(unlift(ServerErrorMessage.unapply))

}

case class BasicServerErrorMessage(user: String, errorCode: ServerErrorCode) extends ServerErrorMessage