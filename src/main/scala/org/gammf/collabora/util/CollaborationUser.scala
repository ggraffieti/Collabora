package org.gammf.collabora.util

import org.gammf.collabora.util.CollaborationRight.CollaborationRight
import play.api.libs.functional.syntax.unlift
import play.api.libs.json.{JsPath, Reads, Writes}
import play.api.libs.functional.syntax._
import reactivemongo.bson.{BSONDocument, BSONDocumentReader, BSONDocumentWriter}



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

  import org.gammf.collabora.database._

  implicit object BSONtoCollaborationUser extends BSONDocumentReader[CollaborationUser] {
    def read(doc: BSONDocument): CollaborationUser = {
      CollaborationUser(
        user = doc.getAs[String](COLLABORATION_USER_USERNAME).get,
        right = doc.getAs[String](COLLABORATION_USER_RIGHT).map(r => CollaborationRight.withName(r)).get
      )
    }
  }

  implicit object CollaborationUserToBSON extends BSONDocumentWriter[CollaborationUser] {
    def write(user: CollaborationUser): BSONDocument = {
      BSONDocument(
        COLLABORATION_USER_USERNAME -> user.user,
        COLLABORATION_USER_RIGHT -> user.right.toString
      )
    }
  }

}
