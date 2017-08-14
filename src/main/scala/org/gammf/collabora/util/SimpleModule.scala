package org.gammf.collabora.util

import play.api.libs.json.{JsPath, Json, Reads, Writes}
import play.api.libs.functional.syntax._
import reactivemongo.bson.{BSONArray, BSONDocument, BSONDocumentReader, BSONDocumentWriter, BSONObjectID}

/**
  * Simple implementation of the trait Module
  * @param id the id of the module (the id is unambiguous inside the collaboration)
  * @param description the title or description of the module
  * @param previousModules previous modules, until every of them is not finished, this module cannot be started
  * @param state the state of the module.
  */
case class SimpleModule(id: Option[String] = None, description: String, previousModules: Option[List[String]] = None, state: String)  extends Module {

}

object SimpleModule {

  implicit val simpleModuleReads: Reads[SimpleModule] = (
    (JsPath \ "id").readNullable[String] and
      (JsPath \ "description").read[String] and
      (JsPath \ "previousModules").readNullable[List[String]] and
      (JsPath \ "state").read[String]
  )(SimpleModule.apply _)

  implicit val simpleModuleWrites: Writes[SimpleModule] = (
    (JsPath \ "id").writeNullable[String] and
      (JsPath \ "description").write[String] and
      (JsPath \ "previousModules").writeNullable[List[String]] and
      (JsPath \ "state").write[String]
  )(unlift(SimpleModule.unapply))

  implicit object BSONtoModule extends BSONDocumentReader[SimpleModule] {
    def read(doc: BSONDocument): SimpleModule = {
      SimpleModule(
        id = doc.getAs[BSONObjectID]("id").map(id => id.stringify),
        description = doc.getAs[String]("description").get,
        previousModules = doc.getAs[List[BSONObjectID]]("previousModules").map(l => l.map(bsonID => bsonID.stringify)),
        state = doc.getAs[String]("state").get
      )
    }
  }

  implicit object ModuletoBSON extends BSONDocumentWriter[SimpleModule] {
    def write(module: SimpleModule): BSONDocument = {
      var bsonModule = BSONDocument()
      if (module.id.isDefined) bsonModule = bsonModule.merge("id" -> BSONObjectID.parse(module.id.get).get)
      else bsonModule = bsonModule.merge("id" -> BSONObjectID.generate())
      bsonModule = bsonModule.merge(BSONDocument("description" -> module.description))
      bsonModule = bsonModule.merge(BSONDocument("state" -> module.state))
      if (module.previousModules.isDefined) {
        val arr = BSONArray(module.previousModules.get.map(e => BSONObjectID.parse(e).get))
        bsonModule = bsonModule.merge(BSONDocument("previousModules" -> arr))
      }
      bsonModule
    }
  }

}

object testModuleImplicits extends App {
  val module = SimpleModule(Option("nome del modulo"),"questo è un modulo importante",Option(List("28fsd7df8273","fs7d7g7sd7","gdfg7sd7fg")),"doing")

  val jsn = Json.toJson(module)
  println("Json format: " + jsn)
  println("Object format:" + jsn.as[SimpleModule])
}

