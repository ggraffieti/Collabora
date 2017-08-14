package org.gammf.collabora.util

import org.gammf.collabora.util.CollaborationType.CollaborationType
import org.joda.time.DateTime
import play.api.libs.json.{JsPath, Json, Reads, Writes}
import play.api.libs.functional.syntax._
import reactivemongo.bson.{BSON, BSONArray, BSONDocument, BSONDocumentReader, BSONDocumentWriter, BSONObjectID}

/**
  * A simple implementation of the trait collaboration
  * @param id the id of the collaboration
  * @param name the name of the collaboration
  * @param collaborationType the type (group, project, private)
  * @param users a list of CollaborationUsers
  * @param modules a list of modules inside this collaboration
  * @param notes a list of notes inside this collaborations
  */
case class SimpleCollaboration(id: Option[String] = None, name: String, collaborationType: CollaborationType,
                               users: Option[List[CollaborationUser]] = None,
                               modules: Option[List[SimpleModule]] = None,
                               notes: Option[List[SimpleNote]] = None) extends Collaboration {

}


object SimpleCollaboration {

  implicit val simpleCollaborationReads: Reads[SimpleCollaboration] = (
    (JsPath \ "id").readNullable[String] and
      (JsPath \ "name").read[String] and
      (JsPath \ "collaborationType").read[CollaborationType] and
      (JsPath \ "users").readNullable[List[CollaborationUser]] and
      (JsPath \ "modules").readNullable[List[SimpleModule]] and
      (JsPath \ "notes").readNullable[List[SimpleNote]]
  )(SimpleCollaboration.apply _)

  implicit val simpleCollaborationWrites: Writes[SimpleCollaboration] = (
    (JsPath \ "id").writeNullable[String] and
      (JsPath \ "name").write[String] and
      (JsPath \ "collaborationType").write[CollaborationType] and
      (JsPath \ "users").writeNullable[List[CollaborationUser]] and
      (JsPath \ "modules").writeNullable[List[SimpleModule]] and
      (JsPath \ "notes").writeNullable[List[SimpleNote]]
  )(unlift(SimpleCollaboration.unapply))

  implicit object BSONtoCollaboration extends BSONDocumentReader[SimpleCollaboration] {
    def read(doc: BSONDocument): SimpleCollaboration = {
      SimpleCollaboration(
        id = doc.getAs[BSONObjectID]("_id").map(id => id.stringify),
        name = doc.getAs[String]("name").get,
        collaborationType = doc.getAs[String]("collaborationType").map(t => CollaborationType.withName(t)).get,
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
      newCollaboration = newCollaboration.merge(BSONDocument("collaborationType" -> collaboration.collaborationType.toString))

      if (collaboration.users.isDefined) {
        val arr = BSONArray(collaboration.users.get.map(e => BSON.write(e)))
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

object CollectionImplicitTest extends App {
  val collaboration = SimpleCollaboration(Option.empty,
                                          "nome",
                                          CollaborationType.GROUP,
                                          Option(List(CollaborationUser("fone", CollaborationRight.ADMIN), CollaborationUser("peru", CollaborationRight.ADMIN))),
                                          Option.empty,
                                          Option(List(SimpleNote(Option("prova"),"questo è il contenuto",Option(new DateTime()),Option(Location(23.32,23.42)),Option.empty,NoteState("doing", Option("fone")),Option.empty),
                                                      SimpleNote(Option("prova2"),"questo è il contenuto2",Option(new DateTime()),Option(Location(233.32,233.42)),Option.empty,NoteState("done", Option("peru")),Option.empty))))
  val jsn = Json.toJson(collaboration)
  println("Json format: " + jsn)
  println("Object format: " + jsn.as[SimpleCollaboration])
}
