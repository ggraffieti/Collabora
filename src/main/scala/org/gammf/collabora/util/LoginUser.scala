package org.gammf.collabora.util

import reactivemongo.bson.{BSONDocument, BSONDocumentReader}

trait LoginUser {

  def username: String
  def hashedPassword: String

}

object LoginUser {

  def apply(username: String, hashedPassword: String) = new SimpleLoginUser(username, hashedPassword)

  implicit object BSONtoLoginUser extends BSONDocumentReader[LoginUser] {
    def read(doc: BSONDocument): LoginUser = {
      LoginUser(
        username = doc.getAs[String]("_id").get,
        hashedPassword = doc.getAs[String]("password").get
      )
    }
  }
}
