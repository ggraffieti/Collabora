package org.gammf.collabora.util

import org.gammf.collabora.util.CollaborationRight.CollaborationRight
import org.gammf.collabora.util.CollaborationType.CollaborationType
import play.api.libs.json._
import play.api.libs.functional.syntax._
import reactivemongo.bson.{BSON, BSONArray, BSONDocument, BSONDocumentReader, BSONDocumentWriter, BSONObjectID}

/**
  * The representation of a collaboration
  */
trait Collaboration {

  def id: Option[String]
  def name: String
  def collaborationType: CollaborationType
  def users: Option[List[CollaborationUser]]
  def modules: Option[List[Module]]
  def notes: Option[List[Note]]

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

object Collaboration {
  def apply(id: Option[String], name: String, collaborationType: CollaborationType, users: Option[List[CollaborationUser]], modules: Option[List[Module]], notes: Option[List[Note]]): Collaboration = SimpleCollaboration(id, name, collaborationType, users, modules, notes)

  def unapply(arg: Collaboration): Option[(Option[String], String, CollaborationType, Option[List[CollaborationUser]], Option[List[Module]], Option[List[Note]])] = Some((arg.id, arg.name, arg.collaborationType, arg.users, arg.modules, arg.notes))

  implicit val collaborationReads: Reads[Collaboration] = (
    (JsPath \ "id").readNullable[String] and
      (JsPath \ "name").read[String] and
      (JsPath \ "collaborationType").read[CollaborationType] and
      (JsPath \ "users").readNullable[List[CollaborationUser]] and
      (JsPath \ "modules").readNullable[List[Module]] and
      (JsPath \ "notes").readNullable[List[Note]]
    )(Collaboration.apply _)

  implicit val collaborationWrites: Writes[Collaboration] = (
    (JsPath \ "id").writeNullable[String] and
      (JsPath \ "name").write[String] and
      (JsPath \ "collaborationType").write[CollaborationType] and
      (JsPath \ "users").writeNullable[List[CollaborationUser]] and
      (JsPath \ "modules").writeNullable[List[Module]] and
      (JsPath \ "notes").writeNullable[List[Note]]
    )(unlift(Collaboration.unapply))

  implicit object BSONtoCollaboration extends BSONDocumentReader[Collaboration] {
    def read(doc: BSONDocument): Collaboration = {
      Collaboration(
        id = doc.getAs[BSONObjectID]("_id").map(id => id.stringify),
        name = doc.getAs[String]("name").get,
        collaborationType = doc.getAs[String]("collaborationType").map(t => CollaborationType.withName(t)).get,
        users = doc.getAs[List[CollaborationUser]]("users"),
        modules = doc.getAs[List[Module]]("modules"),
        notes = doc.getAs[List[Note]]("notes")
      )
    }
  }

  implicit object CollaborationToBSON extends BSONDocumentWriter[Collaboration] {
    def write(collaboration: Collaboration): BSONDocument = {
      BSONDocument(
        "_id" -> { if (collaboration.id.isDefined) BSONObjectID.parse(collaboration.id.get).get else BSONObjectID.generate() },
        "name" -> collaboration.name,
        "collaborationType" -> collaboration.collaborationType.toString,
        { if (collaboration.users.isDefined) "users" -> BSONArray(collaboration.users.get.map(e => BSON.write(e))) else BSONDocument() },
        { if (collaboration.modules.isDefined) "modules" -> BSONArray(collaboration.modules.get) else BSONDocument() },
        { if (collaboration.notes.isDefined) "notes" -> BSONArray(collaboration.notes.get.map(n => BSON.write(n))) else BSONDocument() },
      )
    }
  }
}

