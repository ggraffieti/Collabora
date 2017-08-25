package org.gammf.collabora.util

import play.api.libs.functional.syntax.unlift
import play.api.libs.json.{JsPath, Reads, Writes}
import play.api.libs.functional.syntax._
import reactivemongo.bson.{BSONArray, BSONDocument, BSONDocumentReader, BSONDocumentWriter, BSONObjectID}

/**
  * Represents a module: a group of notes in a project.
  */
trait Module {

  def id: Option[String]
  def description: String
  def previousModules: Option[List[String]]
  def state: String


  override def toString: String = {
    "{ Module -- id=" + id +
      ", content=" + description +
      ", previousModules=" + previousModules +
      ", state=" + state +
    " }"
  }
}

object Module {
  def apply(id: Option[String], description: String, previousModules: Option[List[String]], state: String): Module = SimpleModule(id, description, previousModules, state)

  def unapply(arg: Module): Option[(Option[String], String, Option[List[String]], String)] = Some((arg.id, arg.description, arg.previousModules, arg.state))

  implicit val moduleReads: Reads[Module] = (
    (JsPath \ "id").readNullable[String] and
      (JsPath \ "description").read[String] and
      (JsPath \ "previousModules").readNullable[List[String]] and
      (JsPath \ "state").read[String]
    )(Module.apply _)

  implicit val moduleWrites: Writes[Module] = (
    (JsPath \ "id").writeNullable[String] and
      (JsPath \ "description").write[String] and
      (JsPath \ "previousModules").writeNullable[List[String]] and
      (JsPath \ "state").write[String]
    )(unlift(Module.unapply))

  implicit object BSONtoModule extends BSONDocumentReader[Module] {
    def read(doc: BSONDocument): Module = {
      Module(
        id = doc.getAs[BSONObjectID]("id").map(id => id.stringify),
        description = doc.getAs[String]("description").get,
        previousModules = doc.getAs[List[BSONObjectID]]("previousModules").map(l => l.map(bsonID => bsonID.stringify)),
        state = doc.getAs[String]("state").get
      )
    }
  }

  implicit object ModuletoBSON extends BSONDocumentWriter[Module] {
    def write(module: Module): BSONDocument = {
      BSONDocument(
        "id" -> { if (module.id.isDefined) BSONObjectID.parse(module.id.get).get else BSONObjectID.generate() },
        "description" -> module.description,
        "state" -> module.state,
        { if (module.previousModules.isDefined) "previousModules" -> BSONArray(module.previousModules.get.map(
            e => BSONObjectID.parse(e).get))
          else BSONDocument()
        }
      )
    }
  }
}
