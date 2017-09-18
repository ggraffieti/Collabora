package org.gammf.collabora.database.actors.worker

import akka.actor.{ActorRef, Props, Stash}
import akka.pattern.pipe
import org.gammf.collabora.database._
import org.gammf.collabora.database.messages._
import org.gammf.collabora.util.Module
import org.gammf.collabora.yellowpages.ActorService.{ActorService, DefaultWorker}
import reactivemongo.bson.{BSON, BSONDocument, BSONObjectID}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

import org.gammf.collabora.yellowpages.util.Topic.ActorTopic

/**
  * A worker that performs query on modules.
  */
class DBWorkerModuleActor(override val yellowPages: ActorRef, override val name: String,
                          override val topic: ActorTopic, override val service: ActorService = DefaultWorker)
  extends CollaborationsDBWorker[DBWorkerMessage] with DefaultDBWorker with Stash {

  override def receive: Receive = super.receive orElse ({

    case message: InsertModuleMessage =>
      val bsonModule: BSONDocument = BSON.write(message.module) // necessary conversion, sets the moduleID
      update(
        selector = BSONDocument(COLLABORATION_ID -> BSONObjectID.parse(message.collaborationID).get),
        query = BSONDocument("$push" -> BSONDocument(COLLABORATION_MODULES -> bsonModule)),
        okMessage = QueryOkMessage(InsertModuleMessage(bsonModule.as[Module], message.collaborationID, message.userID)),
        failStrategy = defaultDBWorkerFailStrategy(message.userID)
      ) pipeTo sender

    case message: UpdateModuleMessage =>
      update(
        selector = BSONDocument(
          COLLABORATION_ID -> BSONObjectID.parse(message.collaborationID).get,
          COLLABORATION_MODULES + "." + MODULE_ID -> BSONObjectID.parse(message.module.id.get).get
        ),
        query = BSONDocument("$set" -> BSONDocument(COLLABORATION_MODULES + ".$" -> message.module)),
        okMessage = QueryOkMessage(message),
        failStrategy = defaultDBWorkerFailStrategy(message.userID)
      ) pipeTo sender

    case message: DeleteModuleMessage =>
      update(
        selector = BSONDocument(COLLABORATION_ID -> BSONObjectID.parse(message.collaborationID).get),
        query = BSONDocument("$pull" -> BSONDocument(COLLABORATION_MODULES ->
          BSONDocument(MODULE_ID -> BSONObjectID.parse(message.module.id.get).get))),
        okMessage = QueryOkMessage(message),
        failStrategy = defaultDBWorkerFailStrategy(message.userID)
      ).map {
        case queryOk: QueryOkMessage => deleteAllModuleNotes(message.collaborationID, message.module.id.get, queryOk, message.userID)
        case queryFail: QueryFailMessage => Future.successful(queryFail)
      }.flatten pipeTo sender

  }: Receive)


  private[this] def deleteAllModuleNotes(collaborationId: String, moduleId: String,
                                         messageOk: DBWorkerMessage, username: String): Future[DBWorkerMessage] = {
    update(
      selector = BSONDocument(COLLABORATION_ID -> BSONObjectID.parse(collaborationId).get),
      query = BSONDocument("$pull" -> BSONDocument(COLLABORATION_NOTES ->
        BSONDocument(NOTE_MODULE -> BSONObjectID.parse(moduleId).get)
      )),
      okMessage = messageOk,
      failStrategy = defaultDBWorkerFailStrategy(username)
    )
  }
}
