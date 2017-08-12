package org.gammf.collabora.util

import org.gammf.collabora.util.CollaborationType.CollaborationType
import reactivemongo.bson.{BSONArray, BSONDocument, BSONDocumentReader, BSONDocumentWriter, BSONObjectID}

case class SimpleCollaboration(id: Option[String] = None, name: String, collaborationType: CollaborationType,
                               users: Option[List[CollaborationUser]] = None,
                               modules: Option[List[SimpleModule]] = None,
                               notes: Option[List[SimpleNote]] = None) extends Collaboration {

}


object SimpleCollaboration {

  implicit object BSONtoCollaborationUser extends BSONDocumentReader[CollaborationUser] {
    def read(doc: BSONDocument): CollaborationUser = {
      CollaborationUser(
        user = doc.getAs[String]("user").get,
        right = doc.getAs[String]("right").map(r => CollaborationRight.withName(r)).get
      )
    }
  }

  implicit object BSONtoCollaboration extends BSONDocumentReader[SimpleCollaboration] {
    def read(doc: BSONDocument): SimpleCollaboration = {
      SimpleCollaboration(
        id = doc.getAs[BSONObjectID]("_id").map(id => id.stringify),
        name = doc.getAs[String]("name").get,
        collaborationType = doc.getAs[String]("type").map(t => CollaborationType.withName(t)).get,
        users = doc.getAs[List[CollaborationUser]]("users"),
        modules = doc.getAs[List[SimpleModule]]("modules"),
        notes = doc.getAs[List[SimpleNote]]("notes")
      )
    }
  }

  implicit object CollaborationToBSON extends BSONDocumentWriter[SimpleCollaboration] {
    def write(collaboration: SimpleCollaboration): BSONDocument = {
      var newCollaboration = BSONDocument()
      if (collaboration.id.isDefined) newCollaboration = newCollaboration.merge("_id" -> BSONObjectID.parse(collaboration.id.get).get)
      else newCollaboration = newCollaboration.merge("_id" -> BSONObjectID.generate())
      newCollaboration = newCollaboration.merge(BSONDocument("name" -> collaboration.name))
      newCollaboration = newCollaboration.merge(BSONDocument("type" -> collaboration.collaborationType.toString))


      if (collaboration.users.isDefined) {
        val arr = BSONArray(collaboration.users.get.map(e =>
          BSONDocument("user" -> e.user, "right" -> e.right.toString)
        ))
        newCollaboration = newCollaboration.merge(BSONDocument("users" -> arr))
      }
      if (collaboration.modules.isDefined) {
        val arr = BSONArray(collaboration.modules.get)
        newCollaboration = newCollaboration.merge(BSONDocument("modules" -> arr))
      }
      if (collaboration.notes.isDefined) {
        val arr = BSONArray(collaboration.notes.get)
        newCollaboration = newCollaboration.merge(BSONDocument("notes" -> arr))
      }

      newCollaboration
    }
  }

}
