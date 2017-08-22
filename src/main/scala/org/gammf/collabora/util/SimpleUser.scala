package org.gammf.collabora.util

import org.joda.time.DateTime
import play.api.libs.functional.syntax._
import play.api.libs.json.{JsPath, Json, Reads, Writes}
import reactivemongo.bson.{BSONDocument, BSONDocumentReader, BSONDocumentWriter, BSONObjectID}
import SimpleNote._
/**
  * Simple representation of an immutable user
  * @param id the id of the user
  * @param username the user's username
  * @param email the user's email
  * @param name the user's name
  * @param surname the user's surname
  * @param birthday the user's birthday
  */
case class SimpleUser(id: Option[String] = None,
                      username: String,
                      email: String,
                      name: String,
                      surname: String,
                      birthday: DateTime) extends User{

}

object SimpleUser {
  implicit val simpleUserReads: Reads[SimpleUser] = (
    (JsPath \ "id").readNullable[String] and
      (JsPath \ "username").read[String] and
      (JsPath \ "email").read[String] and
      (JsPath \ "name").read[String] and
      (JsPath \ "surname").read[String] and
      (JsPath \ "birthday").read[DateTime]
  )(SimpleUser.apply _)

  implicit val simpleUserWrites: Writes[SimpleUser] = (
    (JsPath \ "id").writeNullable[String] and
      (JsPath \ "username").write[String] and
      (JsPath \ "email").write[String] and
      (JsPath \ "name").write[String] and
      (JsPath \ "surname").write[String] and
      (JsPath \ "birthday").write[DateTime]
    )(unlift(SimpleUser.unapply))

  implicit object BSONtoUser extends BSONDocumentReader[SimpleUser] {
    override def read(bson: BSONDocument): SimpleUser = {
      SimpleUser(
        id = bson.getAs[BSONObjectID]("_id").map(id => id.stringify),
        username = bson.getAs[String]("username").get,
        email = bson.getAs[String]("email").get,
        name = bson.getAs[String]("name").get,
        surname = bson.getAs[String]("surname").get,
        birthday = bson.getAs[DateTime]("birthday").get
      )
    }
  }

  implicit object UserToBson extends BSONDocumentWriter[SimpleUser] {
    override def write(user: SimpleUser): BSONDocument = {
      var bson = BSONDocument()
      if (user.id.isDefined) bson = bson.merge("_id" -> BSONObjectID.parse(user.id.get).get)
      else bson = bson.merge("id" -> BSONObjectID.generate())
      bson = bson.merge(BSONDocument("username" -> user.username))
      bson = bson.merge(BSONDocument("email" -> user.email))
      bson = bson.merge(BSONDocument("name" -> user.name))
      bson = bson.merge(BSONDocument("surname" -> user.surname))
      bson = bson.merge(BSONDocument("birthday" -> user.birthday.toDate))

      bson
    }
  }
}

object testUserImplicits extends App {
  val user = SimpleUser(Option("sd8fg8sdfg7"), "maffone", "alfredo.maffi@studio.unibo.it", "Alfredo", "Maffi", new DateTime())
  val json = Json.toJson(user)

  println("Json format: " + json)
  println("Object format: " + json.as[SimpleUser])
}