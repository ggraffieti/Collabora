package org.gammf.collabora.database.actors

import akka.actor.{Actor, Stash}
import org.gammf.collabora.database.messages.{AskConnectionMessage, GetConnectionMessage}
import reactivemongo.api.{MongoConnection, MongoDriver}

import scala.util.{Failure, Success}

/**
  * An actor that have the purpose of establishing and mantaining the connection with the Database
  */
class ConnectionManagerActor extends Actor with Stash {

  private[this] var connection: Option[MongoConnection] = None
  private[this] val mongoUri = "mongodb://localhost:27017/collabora?authMode=scram-sha1"

  override def preStart(): Unit = {
    val driver = MongoDriver()
    val parseUri = MongoConnection.parseURI(mongoUri)
    val tryCconnection = parseUri.map(driver.connection)
    tryCconnection match {
      case Success(c) => connection = Some(c)
      case Failure(e) => e.printStackTrace() // TODO now logging stack
    }
  }

  override def receive: Receive = {
    case _ : AskConnectionMessage => sender() ! new GetConnectionMessage(connection.get)
    case _ => // do nothing
  }
}
