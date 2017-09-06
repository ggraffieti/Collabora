package org.gammf.collabora.database.actors

import akka.actor.{ActorRef, Stash}
import akka.pattern.pipe
import org.gammf.collabora.authentication.messages.{LoginMessage, SigninMessage}
import org.gammf.collabora.database.messages._
import org.gammf.collabora.util.User
import reactivemongo.bson.{BSON, BSONDocument}

import scala.concurrent.ExecutionContext.Implicits.global

class DBWorkerAuthentication(connectionManager: ActorRef) extends UsersDBWorker(connectionManager) with Stash {


  override def receive: Receive = {
    case m: GetConnectionMessage =>
      connection = Some(m.connection)
      unstashAll()

    case _ if connection.isEmpty => stash()

    case message: LoginMessage =>
      find(
        selector = BSONDocument("_id" -> message.username),
        okStrategy = bsonDocument =>  {
          if (bsonDocument.isDefined) AuthenticationMessage(Some(bsonDocument.get.as[User]))
          else AuthenticationMessage(None)
        }
      ) pipeTo sender

    case message: SigninMessage =>
      insert(
        document = BSON.write(message.user),
        okMessage = QueryOkMessage(InsertUserMessage(message.user)),
      ) pipeTo sender

    case _ => unhandled(_)
  }
}
