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

object testModuleImplicits extends App {
  val module = SimpleModule(Option("nome del modulo"),"questo è un modulo importante",Option(List("28fsd7df8273","fs7d7g7sd7","gdfg7sd7fg")),"doing")

  val jsn = Json.toJson(module)
  println("Json format: " + jsn)
  println("Object format:" + jsn.as[Module])
}

