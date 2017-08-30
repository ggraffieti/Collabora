package org.gammf.collabora.database.actors

import akka.actor.{ActorRef, Stash}
import akka.pattern.pipe
import org.gammf.collabora.authentication.messages.LoginMessage
import org.gammf.collabora.database.messages.{AuthenticationMessage, GetConnectionMessage}
import org.gammf.collabora.util.LoginUser
import reactivemongo.bson.BSONDocument

import scala.concurrent.ExecutionContext.Implicits.global

class DBWorkerLogin(connectionManager: ActorRef) extends UsersDBWorker(connectionManager) with Stash {


  override def receive: Receive = {
    case m: GetConnectionMessage =>
      connection = Some(m.connection)
      unstashAll()

    case _ if connection.isEmpty => stash()

    case message: LoginMessage =>
      find(
        selector = BSONDocument("_id" -> message.username),
        okStrategy = bsonDocument =>  {
          if (bsonDocument.isDefined) AuthenticationMessage(Some(bsonDocument.get.as[LoginUser]))
          else AuthenticationMessage(None)
        }
      ) pipeTo sender

    case _ => unhandled(_)
  }
}
