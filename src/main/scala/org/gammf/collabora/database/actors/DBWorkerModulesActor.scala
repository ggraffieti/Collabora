package org.gammf.collabora.database.actors

import akka.actor.{ActorRef, Stash}
import akka.pattern.pipe
import org.gammf.collabora.database.messages._
import org.gammf.collabora.util.Module
import reactivemongo.bson.{BSON, BSONDocument, BSONObjectID}

import scala.concurrent.ExecutionContext.Implicits.global

/**
  * A worker that performs query on modules.
  * @param connectionActor the actor that mantains the connection with the DB.
  */
class DBWorkerModulesActor(connectionActor: ActorRef) extends CollaborationsDBWorker(connectionActor) with Stash {

  override def receive: Receive = {

    case m: GetConnectionMessage =>
      connection = Some(m.connection)
      unstashAll()

    case _ if connection.isEmpty => stash()

    case message: InsertModuleMessage =>
      val bsonModule: BSONDocument = BSON.write(message.module) // necessary conversion, sets the moduleID
      update(
        selector = BSONDocument("_id" -> BSONObjectID.parse(message.collaborationID).get),
        query = BSONDocument("$push" -> BSONDocument("modules" -> bsonModule)),
        okMessage = QueryOkMessage(InsertModuleMessage(bsonModule.as[Module], message.collaborationID, message.userID))
      ) pipeTo sender

    case message: UpdateModuleMessage =>
      update(
        selector = BSONDocument(
          "_id" -> BSONObjectID.parse(message.collaborationID).get,
          "modules.id" -> BSONObjectID.parse(message.module.id.get).get
        ),
        query = BSONDocument("$set" -> BSONDocument("modules.$" -> message.module)),
        okMessage = QueryOkMessage(message)
      ) pipeTo sender

    case message: DeleteModuleMessage =>
      update(
        selector = BSONDocument("_id" -> BSONObjectID.parse(message.collaborationID).get),
        query = BSONDocument("$pull" -> BSONDocument("modules" -> BSONDocument("id" -> BSONObjectID.parse(message.module.id.get).get))),
        okMessage = QueryOkMessage(message)
      ) pipeTo sender

  }
}
