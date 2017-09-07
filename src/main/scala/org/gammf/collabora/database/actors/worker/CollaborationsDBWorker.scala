package org.gammf.collabora.database.actors.worker

import akka.actor.ActorRef
import org.gammf.collabora.database.messages.{DBWorkerMessage, QueryFailMessage}
import reactivemongo.bson.BSONDocument

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
  * A DBWorker that performs query on the collaboration collection.
  * @param connectionActor the actor that mantains the connection with the DB.
  */
abstract class CollaborationsDBWorker(connectionActor: ActorRef) extends AbstractDBWorker(connectionActor) {


  /**
    * Check if in the collection is present at least one document that match the selector, and returns a [[DBWorkerMessage]]
    * @param selector the selector used to find the document to update. Note that if more than one document match the selector,
    *                 only one is returned (presumably the first document in the collection that match the selector)
    * @param okStrategy the strategy that have to be used to map the document found to a [[DBWorkerMessage]]. The
    *                   strategy maps from [[ Option[BSONDocument] ]] because the selector shoud not match any document.
    * @param failStrategy the fail strategy that have to be used if somethings went wrong. The default strategy returns a
    *                     [[QueryFailMessage]] that contains the Exception.
    *
    * @return a [[DBWorkerMessage]], representing the success or the failure of the query.
    */
  override protected def find(selector: BSONDocument,
                              okStrategy: Option[BSONDocument] => DBWorkerMessage,
                              failStrategy: PartialFunction[Throwable, DBWorkerMessage]): Future[DBWorkerMessage] = {
    getCollaborationsCollection.map(collaborations =>
      collaborations.find(selector).one[BSONDocument]
    ).flatten.map(result => okStrategy(result))
      .recover(failStrategy)
  }

  /**
    * Perform an update query. An update query is a query that select a document in the collection, and edit it.
    * DO NOT use this methot to insert or delete documents.
    * @param selector the selector used to find the document to update
    * @param query        the new document that have to be inserted in the collection. Note that this document overwrites the document
    *                     found by the selector. Only one document at a time is edited, so if the selector matches more than one document
    *                     only one is edited (presumably the first document in the collection that match the selector)
    * @param okMessage    the message that have to be returned if the query is correcly done
    * @param failStrategy the fail strategy that have to be used if somethings went wrong. The default strategy returns a
    *                     [[org.gammf.collabora.database.messages.QueryFailMessage]] that contains the Exception.
    *
    * @return a DBWorkerMessage, representing the success or the failure of the query
    */
  override protected def update(selector: BSONDocument, query: BSONDocument, okMessage: DBWorkerMessage,
                                failStrategy: PartialFunction[Throwable, DBWorkerMessage]): Future[DBWorkerMessage] = {
    getCollaborationsCollection.map(collaborations =>
      collaborations.update(
        selector = selector,
        update = query
      )
    ).flatten.map(_ => okMessage).recover(failStrategy)
  }


  override protected def insert(document: BSONDocument, okMessage: DBWorkerMessage,
                                failStrategy: PartialFunction[Throwable, DBWorkerMessage]): Future[DBWorkerMessage] = {
    getCollaborationsCollection.map(collaborations =>
      collaborations.insert(document)
    ).flatten.map(_ => okMessage).recover(failStrategy)
  }

  /**
    * Remove a document that match the given selector. If no document match the selector the query is well formed,
    * so this method returns the okMessage but nothing is done in the collection. If more than one document match the
    * selector only one will be deleted (presumably the first document in the collection that match the selector)
    * @param selector     the selector.
    * @param okMessage    the message that have to be returned if the query is correcly done
    * @param failStrategy the fail strategy that have to be used if somethings went wrong. The default strategy returns a
    *                     [[org.gammf.collabora.database.messages.QueryFailMessage]] that contains the Exception.
    *
    * @return a DBWorkerMessage, representing the success or the failure of the query
    */
  override protected def delete(selector: BSONDocument, okMessage: DBWorkerMessage,
                      failStrategy: PartialFunction[Throwable, DBWorkerMessage]): Future[DBWorkerMessage] = {
    getCollaborationsCollection.map(collaborations =>
      collaborations.remove(selector)
    ).flatten.map(_ => okMessage).recover(failStrategy)
  }
}
