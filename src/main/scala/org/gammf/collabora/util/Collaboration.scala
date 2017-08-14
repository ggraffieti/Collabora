package org.gammf.collabora.util

import org.gammf.collabora.util.CollaborationRight.CollaborationRight
import org.gammf.collabora.util.CollaborationType.CollaborationType
import play.api.libs.json._
import play.api.libs.functional.syntax._
import reactivemongo.bson.{BSONDocument, BSONDocumentReader, BSONDocumentWriter}

/**
  * The representation of a collaboration
  */
trait Collaboration {

  def id: Option[String]
  def name: String
  def collaborationType: CollaborationType
  def users: Option[List[CollaborationUser]]
  def modules: Option[List[SimpleModule]]
  def notes: Option[List[SimpleNote]]

  override def toString: String = {
    "{ Collaboration -- id=" + id +
      ", name=" + name +
      ", collaborationType=" + collaborationType +
      ", users=" + users +
      ", modules= " + modules +
      ", notes= " + notes +
    " }"
  }

}

/**
  * Represents a user inside a collaboration.
  * @param user the username
  * @param right the right privilege
  */
case class CollaborationUser(user: String, right: CollaborationRight)

object CollaborationUser {

  implicit val collaborationUserReads: Reads[CollaborationUser] = (
    (JsPath \ "user").read[String] and
      (JsPath \ "right").read[CollaborationRight]
    )(CollaborationUser.apply _)

  implicit val collaborationUserWrites: Writes[CollaborationUser] = (
    (JsPath \ "user").write[String] and
      (JsPath \ "right").write[CollaborationRight]
  )(unlift(CollaborationUser.unapply))

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

/**
  * Represents the type of the collaboration, can be a group, a project or private (private notes)
  */
object CollaborationType extends Enumeration {
  type CollaborationType = Value
  val PRIVATE, GROUP, PROJECT = Value

  implicit val collaborationTypeReads: Reads[CollaborationType] = js =>
    js.validate[String].map[CollaborationType] (stringType =>
      CollaborationType.withName(stringType)
    )

  implicit val collaborationTypeWrites: Writes[CollaborationType] = (collaborationType) => JsString(collaborationType.toString)
}

/**
  * Rights associated to a user in a collaboration.
  */
object CollaborationRight extends Enumeration {
  type CollaborationRight = Value
  val READ, WRITE, ADMIN = Value

  implicit val collaborationRightReads: Reads[CollaborationRight] = js =>
    js.validate[String].map[CollaborationRight](stringRight =>
      CollaborationRight.withName(stringRight)
    )

  implicit val collaborationRightWrite: Writes[CollaborationRight] = (right) =>  JsString(right.toString)
}

object CollaborationUserTest extends App {
  val user = CollaborationUser("peru", CollaborationRight.ADMIN)
  val json = Json.toJson(user)

  println("Json format: " + json)
  println("Object format: " + json.as[CollaborationUser])
}

