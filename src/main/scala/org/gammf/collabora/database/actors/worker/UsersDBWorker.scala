package org.gammf.collabora.database.actors.worker

import reactivemongo.api.Cursor
import reactivemongo.bson.BSONDocument

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
  * A DBWorker that performs query on the user collection.
  * @param connectionActor the actor that mantains the connection with the DB.
  * @tparam T the type returned by query methods, in case of query gone good or bad.
  */
abstract class UsersDBWorker[T] extends AbstractDBWorker[T] {

  /**
    * Check if in the collection is present at least one document that match the selector, and returns a message of generic type T
    * @param selector the selector used to find the document to update. Note that if more than one document match the selector,
    *                 only one is returned (presumably the first document in the collection that match the selector)
    * @param okStrategy the strategy that have to be used to map the document found to a T object.
    * @param failStrategy the fail strategy that have to be used if somethings went wrong.
    *
    * @return a future representation of a message of type T, representing the success or the failure of the query.
    */
  override protected def find(selector: BSONDocument,
                              okStrategy: Option[BSONDocument] => T,
                              failStrategy: PartialFunction[Throwable, T]): Future[T] = {
    getUsersCollection.map(users =>
      users.find(selector).one[BSONDocument]
    ).flatten.map(result => okStrategy(result))
      .recover(failStrategy)
  }

  /**
    * Check if in the user collection exists at least one user that match the selector. If any it returns all of them.
    * @param selector the selector used to find documents.
    * @param okStrategy the strategy that have to be used to map documents found to the generic type T. The
    *                   strategy maps from [[ List[BSONDocument] ]] to T.
    * @param failStrategy the fail strategy that have to be used if somethings went wrong.
    * @return a future representation of a message of generic type type T, representing the success or the failure of the query
    */
  override protected def findAll(selector: BSONDocument, okStrategy: (List[BSONDocument]) => T,
                                 failStrategy: PartialFunction[Throwable, T]): Future[T] = {
    getUsersCollection.map(user =>
    user.find(selector).cursor[BSONDocument]().collect[List](-1,  Cursor.FailOnError[List[BSONDocument]]()))
      .flatten.map(list => okStrategy(list))
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
    * @param failStrategy the fail strategy that have to be used if somethings went wrong.
    *
    * @return a future representation of a message of type T, representing the success or the failure of the query.
    */
  override protected def update(selector: BSONDocument, query: BSONDocument, okMessage: T,
                                failStrategy: PartialFunction[Throwable, T]): Future[T] = {
    getUsersCollection.map(users =>
      users.update(
        selector = selector,
        update = query
      )
    ).flatten.map(_ => okMessage).recover(failStrategy)
  }


  override protected def insert(document: BSONDocument, okMessage: T,
                                failStrategy: PartialFunction[Throwable, T]): Future[T] = {
    getUsersCollection.map(users =>
      users.insert(document)
    ).flatten.map(_ => okMessage).recover(failStrategy)
  }

  /**
    * Remove a document that match the given selector. If no document match the selector the query is well formed,
    * so this method returns the okMessage but nothing is done in the collection. If more than one document match the
    * selector only one will be deleted (presumably the first document in the collection that match the selector)
    * @param selector     the selector.
    * @param okMessage    the message that have to be returned if the query is correcly done
    * @param failStrategy the fail strategy that have to be used if somethings went wrong.
    *
    * @return a future representation of a message of type T, representing the success or the failure of the query.
    */
  override protected def delete(selector: BSONDocument, okMessage: T,
                                failStrategy: PartialFunction[Throwable, T]): Future[T] = {
    getUsersCollection.map(users =>
      users.remove(selector)
    ).flatten.map(_ => okMessage).recover(failStrategy)
  }
}
