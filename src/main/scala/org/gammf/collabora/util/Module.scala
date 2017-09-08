package org.gammf.collabora.util

import play.api.libs.functional.syntax.unlift
import play.api.libs.json.{JsPath, Reads, Writes}
import play.api.libs.functional.syntax._
import reactivemongo.bson.{BSONDocument, BSONDocumentReader, BSONDocumentWriter, BSONObjectID}

/**
  * Represents a module: a group of notes in a project.
  */
trait Module {

  def id: Option[String]
  def description: String
  def state: String


  override def toString: String = {
    "{ Module -- id=" + id +
      ", content=" + description +
      ", state=" + state +
    " }"
  }
}

object Module {
  def apply(id: Option[String], description: String, state: String): Module = SimpleModule(id, description, state)

  def unapply(arg: Module): Option[(Option[String], String, String)] = Some((arg.id, arg.description, arg.state))

  implicit val moduleReads: Reads[Module] = (
    (JsPath \ "id").readNullable[String] and
      (JsPath \ "description").read[String] and
      (JsPath \ "state").read[String]
    )(Module.apply _)

  implicit val moduleWrites: Writes[Module] = (
    (JsPath \ "id").writeNullable[String] and
      (JsPath \ "description").write[String] and
      (JsPath \ "state").write[String]
    )(unlift(Module.unapply))

  import org.gammf.collabora.database._

  implicit object BSONtoModule extends BSONDocumentReader[Module] {
    def read(doc: BSONDocument): Module = {
      Module(
        id = doc.getAs[BSONObjectID](MODULE_ID).map(id => id.stringify),
        description = doc.getAs[String](MODULE_DESCRIPTION).get,
        state = doc.getAs[String](MODULE_STATE).get
      )
    }
  }

  implicit object ModuletoBSON extends BSONDocumentWriter[Module] {
    def write(module: Module): BSONDocument = {
      BSONDocument(
        MODULE_ID -> { if (module.id.isDefined) BSONObjectID.parse(module.id.get).get else BSONObjectID.generate() },
        MODULE_DESCRIPTION -> module.description,
        MODULE_STATE -> module.state,
      )
    }
  }
}

/**
  * Simple implementation of the trait Module
  * @param id the id of the module (the id is unambiguous inside the collaboration)
  * @param description the title or description of the module
  * @param state the state of the module.
  */
case class SimpleModule(id: Option[String] = None, description: String, state: String)  extends Module {
}
