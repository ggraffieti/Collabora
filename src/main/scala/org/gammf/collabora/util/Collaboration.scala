package org.gammf.collabora.util

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

  import org.gammf.collabora.database._

  implicit object BSONtoCollaboration extends BSONDocumentReader[Collaboration] {
    def read(doc: BSONDocument): Collaboration = {
      Collaboration(
        id = doc.getAs[BSONObjectID](COLLABORATION_ID).map(id => id.stringify),
        name = doc.getAs[String](COLLABORATION_NAME).get,
        collaborationType = doc.getAs[String](COLLABORATION_COLLABORATION_TYPE).map(t => CollaborationType.withName(t)).get,
        users = doc.getAs[List[CollaborationUser]](COLLABORATION_USERS),
        modules = doc.getAs[List[Module]](COLLABORATION_MODULES),
        notes = doc.getAs[List[Note]](COLLABORATION_NOTES)
      )
    }
  }

  implicit object CollaborationToBSON extends BSONDocumentWriter[Collaboration] {
    def write(collaboration: Collaboration): BSONDocument = {
      BSONDocument(
        COLLABORATION_ID -> { if (collaboration.id.isDefined) BSONObjectID.parse(collaboration.id.get).get else BSONObjectID.generate() },
        COLLABORATION_NAME -> collaboration.name,
        COLLABORATION_COLLABORATION_TYPE -> collaboration.collaborationType.toString,
        { if (collaboration.users.isDefined) COLLABORATION_USERS -> BSONArray(collaboration.users.get.map(e => BSON.write(e))) else BSONDocument() },
        { if (collaboration.modules.isDefined) COLLABORATION_MODULES -> BSONArray(collaboration.modules.get.map(m => BSON.write(m))) else BSONDocument() },
        { if (collaboration.notes.isDefined) COLLABORATION_NOTES -> BSONArray(collaboration.notes.get.map(n => BSON.write(n))) else BSONDocument() },
      )
    }
  }
}

