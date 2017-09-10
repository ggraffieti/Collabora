package org.gammf.collabora.database.actors.worker

import akka.actor.{ActorRef, Stash}
import akka.pattern.pipe
import org.gammf.collabora.database._
import org.gammf.collabora.database.messages._
import reactivemongo.bson.{BSONDocument, BSONObjectID}

import scala.concurrent.ExecutionContext.Implicits.global

/**
  * A worker that performs query on members.
  * @param connectionActor the actor that mantains the connection with the DB.
  */
class DBWorkerMemberActor(connectionActor: ActorRef) extends CollaborationsDBWorker[DBWorkerMessage](connectionActor) with DefaultDBWorker with Stash {

  override def receive: Receive = {

    case m: GetConnectionMessage =>
      connection = Some(m.connection)
      unstashAll()

    case _ if connection.isEmpty => stash()

    case message: InsertMemberMessage =>
      update(
        selector = BSONDocument(COLLABORATION_ID -> BSONObjectID.parse(message.collaborationID).get),
        query = BSONDocument("$push" -> BSONDocument(COLLABORATION_USERS -> message.user)),
        okMessage = QueryOkMessage(message),
        failStrategy = defaultDBWorkerFailStrategy
      ) pipeTo sender

    case message: UpdateMemberMessage =>
      update(
        selector = BSONDocument(
          COLLABORATION_ID -> BSONObjectID.parse(message.collaborationID).get,
          COLLABORATION_USERS + "." + COLLABORATION_USER_USERNAME -> message.user.user
        ),
        query = BSONDocument("$set" -> BSONDocument(COLLABORATION_USERS + ".$" -> message.user)),
        okMessage = QueryOkMessage(message),
        failStrategy = defaultDBWorkerFailStrategy
      ) pipeTo sender

    case message: DeleteMemberMessage =>
      update(
        selector = BSONDocument(COLLABORATION_ID -> BSONObjectID.parse(message.collaborationID).get),
        query = BSONDocument("$pull" -> BSONDocument(COLLABORATION_USERS -> BSONDocument(COLLABORATION_USER_USERNAME -> message.user.user))),
        okMessage = QueryOkMessage(message),
        failStrategy = defaultDBWorkerFailStrategy
      ) pipeTo sender

  }
}