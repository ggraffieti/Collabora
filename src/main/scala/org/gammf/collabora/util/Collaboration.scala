package org.gammf.collabora.util

import org.gammf.collabora.util.CollaborationRight.CollaborationRight
import org.gammf.collabora.util.CollaborationType.CollaborationType
import reactivemongo.bson.{BSONDocument, BSONDocumentReader, BSONDocumentWriter}

trait Collaboration {

  def id: Option[String]
  def name: String
  def collaborationType: CollaborationType
  def users: Option[List[CollaborationUser]]
  def modules: Option[List[SimpleModule]]
  def notes: Option[List[SimpleNote]]

}

case class CollaborationUser(user: String, right: CollaborationRight)

object CollaborationUser {

  implicit object BSONtoCollaborationUser extends BSONDocumentReader[CollaborationUser] {
    def read(doc: BSONDocument): CollaborationUser = {
      CollaborationUser(
        user = doc.getAs[String]("user").get,
        right = doc.getAs[String]("right").map(r => CollaborationRight.withName(r)).get
      )
    }
  }


  implicit object CollaborationUserToBSON extends BSONDocumentWriter[CollaborationUser] {
    def write(user: CollaborationUser): BSONDocument = {
      BSONDocument(
        "user" -> user.user,
        "right" -> user.right.toString
      )
    }
  }


}

object CollaborationType extends Enumeration {
  type CollaborationType = Value
  val PRIVATE, GROUP, PROJECT = Value
}

object CollaborationRight extends Enumeration {
  type CollaborationRight = Value
  val READ, WRITE, ADMIN = Value
}



