package org.gammf.collabora.util

import reactivemongo.bson.{BSONArray, BSONDocument, BSONDocumentReader, BSONDocumentWriter, BSONObjectID}

case class SimpleModule(id: Option[String] = None, description: String, previousModules: Option[List[String]] = None, state: String)  extends Module {

}

object SimpleModule {

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
