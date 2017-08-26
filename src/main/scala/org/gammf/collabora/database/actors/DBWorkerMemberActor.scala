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
class DBWorkerMemberActor(connectionActor: ActorRef) extends DBWorker(connectionActor) with Stash {

  override def receive: Receive = {

    case m: GetConnectionMessage =>
      connection = Some(m.connection)
      unstashAll()

    case _ if connection.isEmpty => stash()

    case message: InsertUserMessage =>
      getCollaborationsCollection.map(collaborations =>
        collaborations.update(
          selector = BSONDocument("_id" -> BSONObjectID.parse(message.collaborationID).get),
          update = BSONDocument("$push" -> BSONDocument("users" -> message.user))
        )
      ).map(_ => QueryOkMessage(message))
        .recover({ case e: Exception => QueryFailMessage(e) }) pipeTo sender

    case message: UpdateUserMessage =>
      getCollaborationsCollection.map(collaborations =>
        collaborations.update(
          selector = BSONDocument(
            "_id" -> BSONObjectID.parse(message.collaborationID).get,
            "users.username" -> message.user.user
          ),
          update = BSONDocument("$set" -> BSONDocument("users.$" -> message.user))
        )
      ).map(_ => QueryOkMessage(message))
        .recover({ case e: Exception => QueryFailMessage(e) }) pipeTo sender

    case message: DeleteUserMessage =>
      getCollaborationsCollection.map(collaborations =>
        collaborations.update(
          selector = BSONDocument("_id" -> BSONObjectID.parse(message.collaborationID).get),
          update = BSONDocument("$pull" -> BSONDocument("users" ->
            BSONDocument("username" -> message.user.user)))
        )
      ).map(_ => QueryOkMessage(message))
        .recover({ case e: Exception => QueryFailMessage(e) }) pipeTo sender

  }
}
