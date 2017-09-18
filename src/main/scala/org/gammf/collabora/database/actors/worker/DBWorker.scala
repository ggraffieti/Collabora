package org.gammf.collabora.database.actors.worker

import akka.actor.{ActorRef, Props}
import org.gammf.collabora.yellowpages.actors.BasicActor
import org.gammf.collabora.yellowpages.util.Topic.ActorTopic
import reactivemongo.api.MongoConnection
import reactivemongo.api.collections.bson.BSONCollection
import reactivemongo.bson.BSONDocument

import scala.concurrent.Future

/**
  * The representation of a DBWorker. A DBWorker is an actor that performs query, and reply at the applicant actor with a
  * message of generic type T.
  * @tparam T the type returned by query methods, in case of query gone good or bad.
  */
trait DBWorker[T] extends BasicActor {

  override def receive: Receive = super[BasicActor].receive

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
    * @param selector the selector used to find the document
    * @param okStrategy the strategy that have to be used to map the document found to the generic type T. The
    *                   strategy maps from an Option of BSONDocument because the selector shoud not match any document.
    * @param failStrategy the fail strategy that have to be used if somethings went wrong.
    *
    * @return a future representation of a message of generic type type T, representing the success or the failure of the query
    */
  protected def find(selector: BSONDocument,
                       okStrategy: Option[BSONDocument] => T,
                       failStrategy: PartialFunction[Throwable, T]): Future[T]

  /**
    * Check if in the collection exists at least one document that match the selector. If any it returns all of them.
    * @param selector the selector used to find documents.
    * @param okStrategy the strategy that have to be used to map documents found to the generic type T. The
    *                   strategy maps from a List of BSONDocument to T.
    * @param failStrategy the fail strategy that have to be used if somethings went wrong.
    * @return a future representation of a message of generic type type T, representing the success or the failure of the query
    */
  protected def findAll(selector: BSONDocument,
                        okStrategy: List[BSONDocument] => T,
                        failStrategy: PartialFunction[Throwable, T]): Future[T]

  /**
    * Perform an update query. An update query is a query that select a document in the collection, and edit it.
    * DO NOT use this methot to insert or delete documents.
    * @param selector the selector used to find the document to update
    * @param query the new document that have to be inserted in the collection, overwrite the ones found by the selector.
    * @param okMessage the message that have to be returned if the query is correcly done
    * @param failStrategy the fail strategy that have to be used if somethings went wrong.
    *
    * @return a future representation of a message of generic type type T, representing the success or the failure of the query
    */
  protected def update(selector: BSONDocument,
             query: BSONDocument,
             okMessage: T,
             failStrategy: PartialFunction[Throwable, T]): Future[T]

  /**
    * Insert the document in the collection
    * @param document the document that will be inserted in the collection
    * @param okMessage the message that have to be returned if the query is correcly done
    * @param failStrategy the fail strategy that have to be used if somethings went wrong.
    *
    * @return a future representation of a message of generic type T, representing the success or the failure of the query
    */
  protected def insert(document: BSONDocument,
             okMessage: T,
             failStrategy: PartialFunction[Throwable, T]): Future[T]

  /**
    * Remove a document that match the given selector.
    * @param selector the selector.
    * @param okMessage the message that have to be returned if the query is correcly done
    * @param failStrategy the fail strategy that have to be used if somethings went wrong.
    *
    * @return a future representation of a message of generic type T, representing the success or the failure of the query
    */
  protected def delete(selector: BSONDocument,
             okMessage: T,
             failStrategy: PartialFunction[Throwable, T]): Future[T]
}

object DBWorker {
  /**
    * Factory method that returns a Props to create an already-registered database worker authentication actor.
    * @param yellowPages the reference to the yellow pages root actor.
    * @param topic the topic to which this actor is going to be registered.
    * @return the Props to use to create a database worker authentication actor.
    */
  def dbWorkerAuthenticationProps(yellowPages: ActorRef, topic: ActorTopic, name: String = "DBWorkerAuthentication") : Props =
    Props(new DBWorkerAuthenticationActor(yellowPages = yellowPages, name = name, topic = topic))

  /**
    * Factory method that returns a Props to create an already-registered database worker change module state actor.
    * @param yellowPages the reference to the yellow pages root actor.
    * @param topic the topic to which this actor is going to be registered.
    * @return the Props to use to create a database worker change module state actor.
    */
  def dbWorkerChangeModuleStateProps(yellowPages: ActorRef, topic: ActorTopic, name: String = "DBWorkerChangeModuleState") : Props =
    Props(new DBWorkerChangeModuleStateActor(yellowPages = yellowPages, name = name, topic = topic))

  /**
    * Factory method that returns a Props to create an already-registered database worker check member existence actor.
    * @param yellowPages the reference to the yellow pages root actor.
    * @param topic the topic to which this actor is going to be registered.
    * @return the Props to use to create a database worker check member existence actor.
    */
  def dbWorkerCheckMemberExistenceProps(yellowPages: ActorRef, topic: ActorTopic, name: String = "DBWorkerCheckMember") : Props =
    Props(new DBWorkerCheckMemberExistenceActor(yellowPages = yellowPages, name = name, topic = topic))

  /**
    * Factory method that returns a Props to create an already-registered database worker collaborations actor.
    * @param yellowPages the reference to the yellow pages root actor.
    * @param topic the topic to which this actor is going to be registered.
    * @return the Props to use to create a database worker collaborations actor.
    */
  def dbWorkerCollaborationProps(yellowPages: ActorRef, topic: ActorTopic, name: String = "DBWorkerCollaborations") : Props =
    Props(new DBWorkerCollaborationActor(yellowPages = yellowPages, name = name, topic = topic))

  /**
    * Factory method that returns a Props to create an already-registered database worker get collaboration actor.
    * @param yellowPages the reference to the yellow pages root actor.
    * @param topic the topic to which this actor is going to be registered.
    * @return the Props to use to create a database worker get collaboration actor.
    */
  def dbWorkerGetCollaborationProps(yellowPages: ActorRef, topic: ActorTopic, name: String = "DBWorkerGetCollaboration") : Props =
    Props(new DBWorkerGetCollaborationActor(yellowPages = yellowPages, name = name, topic = topic))

  /**
    * Factory method that returns a Props to create an already-registered database worker member actor.
    * @param yellowPages the reference to the yellow pages root actor.
    * @param topic the topic to which this actor is going to be registered.
    * @return the Props to use to create a database worker member actor.
    */
  def dbWorkerMemberProps(yellowPages: ActorRef, topic: ActorTopic, name: String = "DBWorkerMember") : Props =
    Props(new DBWorkerMemberActor(yellowPages = yellowPages, name = name, topic = topic))

  /**
    * Factory method that returns a Props to create an already-registered database worker modules actor.
    * @param yellowPages the reference to the yellow pages root actor.
    * @param topic the topic to which this actor is going to be registered.
    * @return the Props to use to create a database worker modules actor.
    */
  def dbWorkerModuleProps(yellowPages: ActorRef, topic: ActorTopic, name: String = "DBWorkerModules") : Props =
    Props(new DBWorkerModuleActor(yellowPages = yellowPages, name = name, topic = topic))

  /**
    * Factory method that returns a Props to create an already-registered database worker notes actor.
    * @param yellowPages the reference to the yellow pages root actor.
    * @param topic the topic to which this actor is going to be registered.
    * @return the Props to use to create a database worker notes actor.
    */
  def dbWorkerNoteProps(yellowPages: ActorRef, topic: ActorTopic, name: String = "DBWorkerNotes") : Props =
    Props(new DBWorkerNoteActor(yellowPages = yellowPages, name = name, topic = topic))
}
