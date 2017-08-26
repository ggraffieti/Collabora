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
class DBWorkerModulesActor(connectionActor: ActorRef) extends DBWorker(connectionActor) with Stash {

  override def receive: Receive = {

    case m: GetConnectionMessage =>
      connection = Some(m.connection)
      unstashAll()

    case _ if connection.isEmpty => stash()

    case message: InsertModuleMessage =>
      val bsonModule: BSONDocument = BSON.write(message.module) // necessary conversion, sets the moduleID
      getCollaborationsCollection.map(collaborations =>
        collaborations.update(
          selector = BSONDocument("_id" -> BSONObjectID.parse(message.collaborationID).get),
          update = BSONDocument("$push" -> BSONDocument("modules" -> bsonModule))
        )
      ).map(_ => QueryOkMessage(InsertModuleMessage(bsonModule.as[Module], message.collaborationID, message.userID)))
        .recover({ case e: Exception => QueryFailMessage(e) }) pipeTo sender

    case message: UpdateModuleMessage =>
      getCollaborationsCollection.map(collaborations =>
        collaborations.update(
          selector = BSONDocument(
            "_id" -> BSONObjectID.parse(message.collaborationID).get,
            "modules.id" -> BSONObjectID.parse(message.module.id.get).get
          ),
          update = BSONDocument("$set" -> BSONDocument("modules.$" -> message.module))
        )
      ).map(_ => QueryOkMessage(message))
        .recover({ case e: Exception => QueryFailMessage(e) }) pipeTo sender

    case message: DeleteModuleMessage =>
      getCollaborationsCollection.map(collaborations =>
        collaborations.update(
          selector = BSONDocument("_id" -> BSONObjectID.parse(message.collaborationID).get),
          update = BSONDocument("$pull" -> BSONDocument("modules" ->
          BSONDocument("id" -> BSONObjectID.parse(message.module.id.get).get)))
        )
      ).map(_ => QueryOkMessage(message))
        .recover({ case e: Exception => QueryFailMessage(e) }) pipeTo sender

  }
}
