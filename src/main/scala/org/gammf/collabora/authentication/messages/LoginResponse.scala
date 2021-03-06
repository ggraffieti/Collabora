package org.gammf.collabora.authentication.messages

import org.gammf.collabora.util.{Collaboration, User}
import play.api.libs.functional.syntax.unlift
import play.api.libs.json.{JsPath, Writes}
import play.api.libs.functional.syntax._

trait LoginResponse {
  def user: User
  def collaborations: Option[List[Collaboration]]
}

/**
  * A response to the [[LoginMessage]]. It contains the user info and the list of collaborations where
  * the user is present.
  */
object LoginResponse {

  def apply(user: User, collaborations: Option[List[Collaboration]]): LoginResponse = SimpleLoginReponse(user, collaborations)

  def unapply(arg: LoginResponse): Option[(User, Option[List[Collaboration]])] = Some((arg.user, arg.collaborations))

  implicit val loginResponseWrites: Writes[LoginResponse] = (
    (JsPath \ "user").write[User] and
      (JsPath \ "collaborations").writeNullable[List[Collaboration]]
    ) (unlift(LoginResponse.unapply))

}


/**
  * Basic implementation of [[LoginResponse]]
  * @param user the [[org.gammf.collabora.util.User]] object, containing info about the logged user
  * @param collaborations a list of user's collaboartions
  */
case class SimpleLoginReponse(user: User, collaborations: Option[List[Collaboration]]) extends LoginResponse
