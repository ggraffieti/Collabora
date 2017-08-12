package org.gammf.collabora.util

import reactivemongo.bson.{BSONArray, BSONDocument, BSONDocumentReader, BSONDocumentWriter, BSONObjectID}

case class SimpleModule(id: Option[String] = None, content: String, previousModules: Option[List[String]] = None, state: String)  extends Module {

}

object SimpleModule {

  implicit object BSONtoModule extends BSONDocumentReader[Module] {
    def read(doc: BSONDocument): Module = {
      SimpleModule(
        id = doc.getAs[BSONObjectID]("id").map(id => id.stringify),
        content = doc.getAs[String]("text").get,  // TODO maybe change this in content??
        previousModules = doc.getAs[List[BSONObjectID]]("previousModules").map(l => l.map(bsonID => bsonID.stringify)),
        state = doc.getAs[String]("state").get
      )
    }
  }

  implicit object ModuletoBSON extends BSONDocumentWriter[Module] {
    def write(module: Module): BSONDocument = {
      var bsonModule = BSONDocument()
      if (module.id.isDefined) bsonModule = bsonModule.merge("id" -> BSONObjectID.parse(module.id.get).get)
      else bsonModule = bsonModule.merge("id" -> BSONObjectID.generate())
      bsonModule = bsonModule.merge(BSONDocument("text" -> module.content))  // TODO maybe change this in content??
      bsonModule = bsonModule.merge(BSONDocument("state" -> module.state))
      if (module.previousModules.isDefined) {
        val arr = BSONArray(module.previousModules.get.map(e => BSONObjectID.parse(e).get))
        bsonModule = bsonModule.merge(BSONDocument("previousModules" -> arr))
      }
      bsonModule
    }
  }

}
