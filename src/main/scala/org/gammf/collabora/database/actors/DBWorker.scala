package org.gammf.collabora.database.actors

import akka.actor.Actor
import org.gammf.collabora.database.messages.{DBWorkerMessage, QueryFailMessage}
import reactivemongo.api.MongoConnection
import reactivemongo.api.collections.bson.BSONCollection
import reactivemongo.bson.BSONDocument

import scala.concurrent.Future

trait DBWorker extends Actor {

  private[this] val defaultFailStrategy: PartialFunction[Throwable, DBWorkerMessage] = { case e: Exception => QueryFailMessage(e) }


  protected def connection: Option[MongoConnection]
  protected def getCollaborationsCollection: Future[BSONCollection]
  protected def getUsersCollection: Future[BSONCollection]
  protected def update(selector: BSONDocument,
             query: BSONDocument,
             okMessage: DBWorkerMessage,
             failStrategy: PartialFunction[Throwable, DBWorkerMessage] = defaultFailStrategy): Future[DBWorkerMessage]
  protected def insert(document: BSONDocument,
             okMessage: DBWorkerMessage,
             failStrategy: PartialFunction[Throwable, DBWorkerMessage] = defaultFailStrategy): Future[DBWorkerMessage]
  protected def delete(selector: BSONDocument,
             okMessage: DBWorkerMessage,
             failStrategy: PartialFunction[Throwable, DBWorkerMessage] = defaultFailStrategy): Future[DBWorkerMessage]
}
