package org.gammf.collabora.database.actors.worker

import akka.actor.Stash
import org.gammf.collabora.database._
import org.gammf.collabora.database.messages.{AskConnectionMessage, GetConnectionMessage}
import org.gammf.collabora.yellowpages.ActorService.ConnectionHandler
import org.gammf.collabora.yellowpages.util.Topic
import reactivemongo.api.collections.bson.BSONCollection
import reactivemongo.api.{FailoverStrategy, MongoConnection}

import org.gammf.collabora.yellowpages.TopicElement._
import org.gammf.collabora.yellowpages.messages.RegistrationResponseMessage

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
  * A DB worker, it ask for a connection at start time and perform queries.
  * @tparam T the type returned by query methods, in case of query gone good or bad.
  */
abstract class AbstractDBWorker[T] extends DBWorker[T] with Stash {

  protected var connection: Option[MongoConnection] = None

  override def receive: Receive = {

    case message: RegistrationResponseMessage => getActorOrElse(Topic() :+ Database, ConnectionHandler, message)
      .foreach(_ ! AskConnectionMessage())

    case message: GetConnectionMessage =>
      connection = Some(message.connection)
      unstashAll()

    case _ if connection.isEmpty => stash()

  }

  protected def getCollaborationsCollection: Future[BSONCollection] =
    connection.get.database(DB_NAME, FailoverStrategy())
      .map(_.collection(COLLABORATION_CONNECTION_NAME, FailoverStrategy()))

  protected def getUsersCollection: Future[BSONCollection] =
    connection.get.database(DB_NAME, FailoverStrategy())
      .map(_.collection(USER_COLLECTION_NAME, FailoverStrategy()))
}