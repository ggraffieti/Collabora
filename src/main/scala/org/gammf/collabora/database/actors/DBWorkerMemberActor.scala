package org.gammf.collabora.database.actors

import akka.actor.{ActorRef, Stash}
import akka.pattern.pipe
import org.gammf.collabora.database.messages._
import reactivemongo.bson.{BSONDocument, BSONObjectID}

import scala.concurrent.ExecutionContext.Implicits.global

/**
  * A worker that performs query on members.
  * @param connectionActor the actor that mantains the connection with the DB.
  */
class DBWorkerMemberActor(connectionActor: ActorRef) extends CollaborationsDBWorker(connectionActor) with Stash {

  override def receive: Receive = {

    case m: GetConnectionMessage =>
      connection = Some(m.connection)
      unstashAll()

    case _ if connection.isEmpty => stash()

    case message: InsertUserMessage =>
      update(
        selector = BSONDocument("_id" -> BSONObjectID.parse(message.collaborationID).get),
        query = BSONDocument("$push" -> BSONDocument("users" -> message.user)),
        okMessage = QueryOkMessage(message)
      ) pipeTo sender

    case message: UpdateUserMessage =>
      update(
        selector = BSONDocument(
          "_id" -> BSONObjectID.parse(message.collaborationID).get,
          "users.user" -> message.user.user
        ),
        query = BSONDocument("$set" -> BSONDocument("users.$" -> message.user)),
        okMessage = QueryOkMessage(message)
      ) pipeTo sender

    case message: DeleteUserMessage =>
      update(
        selector = BSONDocument("_id" -> BSONObjectID.parse(message.collaborationID).get),
        query = BSONDocument("$pull" -> BSONDocument("users" -> BSONDocument("user" -> message.user.user))),
        okMessage = QueryOkMessage(message)
      ) pipeTo sender

  }
}