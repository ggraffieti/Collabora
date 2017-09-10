package org.gammf.collabora.database.actors.worker

import akka.actor.ActorRef
import org.gammf.collabora.database._
import org.gammf.collabora.database.messages.AskConnectionMessage
import reactivemongo.api.collections.bson.BSONCollection
import reactivemongo.api.{FailoverStrategy, MongoConnection}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
  * A DB worker, it ask for a connection at start time and perform queries.
  * @param connectionActor the actor that mantains the connection with the DB.
  */
abstract class AbstractDBWorker(val connectionActor: ActorRef) extends DBWorker {

  protected var connection: Option[MongoConnection] = None

  override def preStart(): Unit = connectionActor ! AskConnectionMessage()

  protected def getCollaborationsCollection: Future[BSONCollection] =
    connection.get.database(DB_NAME, FailoverStrategy())
      .map(_.collection(COLLABORATION_CONNECTION_NAME, FailoverStrategy()))

  protected def getUsersCollection: Future[BSONCollection] =
    connection.get.database(DB_NAME, FailoverStrategy())
      .map(_.collection(USER_COLLECTION_NAME, FailoverStrategy()))

}