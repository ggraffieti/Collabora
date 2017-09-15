package org.gammf.collabora.util

import org.joda.time.DateTime
import play.api.libs.functional.syntax._
import play.api.libs.json._
import reactivemongo.bson.{BSONDocument, BSONDocumentReader, BSONDocumentWriter}

import org.gammf.collabora.database._

/**
  * Simple trait representing a user of Collabora
  */
trait User {
  def username: String
  def email: String
  def name: String
  def surname: String
  def birthday: DateTime
  def hashedPassword: String

  override def toString: String = {
    "{ User -- username= " + username +
      ", email= " + email +
      ", name= " + name +
      ", surname= " + surname +
      ", birthday= " + birthday +
      " }"
  }

}

object User {

  def apply(username: String, email: String, name: String, surname: String, birthday: DateTime, hashedPassword: String): User =
    SimpleUser(username, email, name, surname, birthday, hashedPassword)

  def unapply(arg: User): Option[(String, String, String, String, DateTime, String)] =
    Some(arg.username, arg.email, arg.name, arg.surname, arg.birthday, arg.hashedPassword)


  implicit val userReads: Reads[User] = (
    (JsPath \ "username").read[String] and
      (JsPath \ "email").read[String] and
      (JsPath \ "name").read[String] and
      (JsPath \ "surname").read[String] and
      (JsPath \ "birthday").read[DateTime] and
      (JsPath \ "hashedPassword").read[String]
    )(User.apply _)

  // not write hashed password in JSON !
  implicit val userWrites: Writes[User] = Writes { user =>
    Json.obj(
      "username" -> user.username,
      "email" -> user.email,
      "name" -> user.name,
      "surname" -> user.surname,
      "birthday" -> user.birthday
    )
  }

  implicit object BSONtoUser extends BSONDocumentReader[User] {
    override def read(bson: BSONDocument): User = {
      User(
        username = bson.getAs[String](USER_ID).get,
        email = bson.getAs[String](USER_EMAIL).get,
        name = bson.getAs[String](USER_NAME).get,
        surname = bson.getAs[String](USER_SURNAME).get,
        birthday = bson.getAs[DateTime](USER_BIRTHDAY).get,
        hashedPassword = bson.getAs[String](USER_PASSWORD).get
      )
    }
  }

  implicit object UserToBson extends BSONDocumentWriter[User] {
    override def write(user: User): BSONDocument = {
      BSONDocument(
        USER_ID -> user.username,
        USER_EMAIL -> user.email,
        USER_NAME -> user.name,
        USER_SURNAME -> user.surname,
        USER_BIRTHDAY -> user.birthday.toDate,
        USER_PASSWORD -> user.hashedPassword
      )
    }
  }
}

case class SimpleUser(username: String,
                      email: String,
                      name: String,
                      surname: String,
                      birthday: DateTime,
                      hashedPassword: String) extends User{

}
