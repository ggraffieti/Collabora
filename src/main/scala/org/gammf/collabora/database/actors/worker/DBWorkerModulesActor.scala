package org.gammf.collabora.database.actors.worker

import akka.actor.{ActorRef, Stash}
import akka.pattern.pipe
import org.gammf.collabora.database._
import org.gammf.collabora.database.messages._
import org.gammf.collabora.util.Module
import reactivemongo.bson.{BSON, BSONDocument, BSONObjectID}

import scala.concurrent.ExecutionContext.Implicits.global

/**
  * A worker that performs query on modules.
  * @param connectionActor the actor that mantains the connection with the DB.
  */
class DBWorkerModulesActor(connectionActor: ActorRef) extends CollaborationsDBWorker[DBWorkerMessage](connectionActor) with Stash {

  private[this] val defaultFailStrategy: PartialFunction[Throwable, DBWorkerMessage] = { case e: Exception => QueryFailMessage(e) }

  override def receive: Receive = {

    case m: GetConnectionMessage =>
      connection = Some(m.connection)
      unstashAll()

    case _ if connection.isEmpty => stash()

    case message: InsertModuleMessage =>
      val bsonModule: BSONDocument = BSON.write(message.module) // necessary conversion, sets the moduleID
      update(
        selector = BSONDocument(COLLABORATION_ID -> BSONObjectID.parse(message.collaborationID).get),
        query = BSONDocument("$push" -> BSONDocument(COLLABORATION_MODULES -> bsonModule)),
        okMessage = QueryOkMessage(InsertModuleMessage(bsonModule.as[Module], message.collaborationID, message.userID)),
        failStrategy = defaultFailStrategy
      ) pipeTo sender

    case message: UpdateModuleMessage =>
      update(
        selector = BSONDocument(
          COLLABORATION_ID -> BSONObjectID.parse(message.collaborationID).get,
          COLLABORATION_MODULES + "." + MODULE_ID -> BSONObjectID.parse(message.module.id.get).get
        ),
        query = BSONDocument("$set" -> BSONDocument(COLLABORATION_MODULES + ".$" -> message.module)),
        okMessage = QueryOkMessage(message),
        failStrategy = defaultFailStrategy
      ) pipeTo sender

    case message: DeleteModuleMessage =>
      update(
        selector = BSONDocument(COLLABORATION_ID -> BSONObjectID.parse(message.collaborationID).get),
        query = BSONDocument("$pull" -> BSONDocument(COLLABORATION_MODULES ->
          BSONDocument(MODULE_ID -> BSONObjectID.parse(message.module.id.get).get))),
        okMessage = QueryOkMessage(message),
        failStrategy = defaultFailStrategy
      ) pipeTo sender
  }
}
