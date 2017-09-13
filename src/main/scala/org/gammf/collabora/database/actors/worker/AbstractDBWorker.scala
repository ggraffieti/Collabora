package org.gammf.collabora.database.actors.worker

import org.gammf.collabora.database._
import reactivemongo.api.collections.bson.BSONCollection
import reactivemongo.api.{FailoverStrategy, MongoConnection}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
  * A DB worker, it ask for a connection at start time and perform queries.
  * @tparam T the type returned by query methods, in case of query gone good or bad.
  */
abstract class AbstractDBWorker[T] extends DBWorker[T] {

  protected var connection: Option[MongoConnection] = None

  override def receive: Receive = super[DBWorker].receive

  protected def getCollaborationsCollection: Future[BSONCollection] =
    connection.get.database(DB_NAME, FailoverStrategy())
      .map(_.collection(COLLABORATION_CONNECTION_NAME, FailoverStrategy()))

  protected def getUsersCollection: Future[BSONCollection] =
    connection.get.database(DB_NAME, FailoverStrategy())
      .map(_.collection(USER_COLLECTION_NAME, FailoverStrategy()))
}