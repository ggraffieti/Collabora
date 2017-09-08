package org.gammf.collabora.util

import org.joda.time.DateTime
import play.api.libs.functional.syntax._
import play.api.libs.json._
import reactivemongo.bson.{BSONDocument, BSONDocumentReader, BSONDocumentWriter}

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
        username = bson.getAs[String]("_id").get,
        email = bson.getAs[String]("email").get,
        name = bson.getAs[String]("name").get,
        surname = bson.getAs[String]("surname").get,
        birthday = bson.getAs[DateTime]("birthday").get,
        hashedPassword = bson.getAs[String]("password").get
      )
    }
  }

  implicit object UserToBson extends BSONDocumentWriter[User] {
    override def write(user: User): BSONDocument = {
      BSONDocument(
        "_id" -> user.username,
        "email" -> user.email,
        "name" -> user.name,
        "surname" -> user.surname,
        "birthday" -> user.birthday.toDate,
        "password" -> user.hashedPassword
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
