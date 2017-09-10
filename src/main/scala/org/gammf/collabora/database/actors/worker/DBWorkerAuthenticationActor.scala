package org.gammf.collabora.database.actors.worker

import akka.actor.{ActorRef, Stash}
import akka.pattern.pipe
import org.gammf.collabora.authentication.messages.{LoginMessage, SigninMessage}
import org.gammf.collabora.database.messages._
import org.gammf.collabora.util.User
import reactivemongo.bson.{BSON, BSONDocument}

import org.gammf.collabora.database._

import scala.concurrent.ExecutionContext.Implicits.global

/**
  * A [[DBWorker]] used for authentication purpose. It manages [[LoginMessage]] and [[SigninMessage]].
  * @param connectionManager the manager of the connection, needed to mantain a stable connection with the database.
  */
class DBWorkerAuthenticationActor(connectionManager: ActorRef) extends UsersDBWorker[DBWorkerMessage](connectionManager) with DefaultDBWorker with Stash {

  override def receive: Receive = {
    case m: GetConnectionMessage =>
      connection = Some(m.connection)
      unstashAll()

    case _ if connection.isEmpty => stash()

    case message: LoginMessage =>
      find(
        selector = BSONDocument(USER_ID -> message.username),
        okStrategy = bsonDocument =>  {
          if (bsonDocument.isDefined) AuthenticationMessage(Some(bsonDocument.get.as[User]))
          else AuthenticationMessage(None)
        },
        failStrategy = defaultDBWorkerFailStrategy
      ) pipeTo sender

    case message: SigninMessage =>
      insert(
        document = BSON.write(message.user),
        okMessage = QueryOkMessage(InsertUserMessage(message.user)),
        failStrategy = defaultDBWorkerFailStrategy
      ) pipeTo sender

    case _ => unhandled(_)
  }
}
