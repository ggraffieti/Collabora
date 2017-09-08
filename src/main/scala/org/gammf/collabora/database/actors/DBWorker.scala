package org.gammf.collabora.database.actors

import akka.actor.Actor
import org.gammf.collabora.database.messages.{DBWorkerMessage, QueryFailMessage}
import reactivemongo.api.MongoConnection
import reactivemongo.api.collections.bson.BSONCollection
import reactivemongo.bson.BSONDocument

import scala.concurrent.Future

/**
  * The representation of a DBWorker. A DBWorker is an actor that performs query, and reply at the applicant actor with a
  * DBWorkerMessage.
  */
trait DBWorker extends Actor {

  private[this] val defaultFailStrategy: PartialFunction[Throwable, DBWorkerMessage] = { case e: Exception => QueryFailMessage(e) }


  /**
    * @return the database connection
    */
  protected def connection: Option[MongoConnection]

  /**
    * @return a future that contains a connection with the collaborations collection.
    */
  protected def getCollaborationsCollection: Future[BSONCollection]
  /**
    * @return a future that contains a connection with the user collection.
    */
  protected def getUsersCollection: Future[BSONCollection]

  /**
    * Check if in the collection is present at least one document that match the selector,
    * @param selector the selector used to find the document to update
    * @param okStrategy the strategy that have to be used to map the document found to a [[DBWorkerMessage]]. The
    *                   strategy maps from [[ Option[BSONDocument] ]] because the selector shoud not match any document.
    * @param failStrategy the fail strategy that have to be used if somethings went wrong. The default strategy returns a
    *                     [[QueryFailMessage]] that contains the Exception.
    *
    * @return a DBWorkerMessage, representing the success or the failure of the query
    */
  protected def find(selector: BSONDocument,
                       okStrategy: Option[BSONDocument] => DBWorkerMessage,
                       failStrategy: PartialFunction[Throwable, DBWorkerMessage] = defaultFailStrategy): Future[DBWorkerMessage]

  /**
    * Perform an update query. An update query is a query that select a document in the collection, and edit it.
    * DO NOT use this methot to insert or delete documents.
    * @param selector the selector used to find the document to update
    * @param query the new document that have to be inserted in the collection, overwrite the ones found by the selector.
    * @param okMessage the message that have to be returned if the query is correcly done
    * @param failStrategy the fail strategy that have to be used if somethings went wrong. The default strategy returns a
    *                     [[QueryFailMessage]] that contains the Exception.
    *
    * @return a DBWorkerMessage, representing the success or the failure of the query
    */
  protected def update(selector: BSONDocument,
             query: BSONDocument,
             okMessage: DBWorkerMessage,
             failStrategy: PartialFunction[Throwable, DBWorkerMessage] = defaultFailStrategy): Future[DBWorkerMessage]

  /**
    * Insert the document in the collection
    * @param document the document that will be inserted in the collection
    * @param okMessage the message that have to be returned if the query is correcly done
    * @param failStrategy the fail strategy that have to be used if somethings went wrong. The default strategy returns a
    *                     [[QueryFailMessage]] that contains the Exception.
    *
    * @return a DBWorkerMessage, representing the success or the failure of the query
    */
  protected def insert(document: BSONDocument,
             okMessage: DBWorkerMessage,
             failStrategy: PartialFunction[Throwable, DBWorkerMessage] = defaultFailStrategy): Future[DBWorkerMessage]

  /**
    * Remove a document that match the given selector.
    * @param selector the selector.
    * @param okMessage the message that have to be returned if the query is correcly done
    * @param failStrategy the fail strategy that have to be used if somethings went wrong. The default strategy returns a
    *                     [[QueryFailMessage]] that contains the Exception.
    *
    * @return a DBWorkerMessage, representing the success or the failure of the query
    */
  protected def delete(selector: BSONDocument,
             okMessage: DBWorkerMessage,
             failStrategy: PartialFunction[Throwable, DBWorkerMessage] = defaultFailStrategy): Future[DBWorkerMessage]
}
