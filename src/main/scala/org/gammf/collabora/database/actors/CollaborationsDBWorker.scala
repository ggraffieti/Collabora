package org.gammf.collabora.database.actors

import akka.actor.ActorRef
import org.gammf.collabora.database.messages.{DBWorkerMessage, QueryFailMessage}
import reactivemongo.bson.BSONDocument

import scala.concurrent.ExecutionContext.Implicits.global

import scala.concurrent.Future

abstract class CollaborationsDBWorker(connectionActor: ActorRef) extends AbstractDBWorker(connectionActor) {

  override protected def update(selector: BSONDocument, query: BSONDocument, okMessage: DBWorkerMessage,
                                failStrategy: PartialFunction[Throwable, DBWorkerMessage]): Future[DBWorkerMessage] = {
    getCollaborationsCollection.map(collaborations =>
      collaborations.update(
        selector = selector,
        update = query
      )
    ).map(_ => okMessage).recover(failStrategy)
  }

  override protected def insert(document: BSONDocument, okMessage: DBWorkerMessage,
                                failStrategy: PartialFunction[Throwable, DBWorkerMessage]): Future[DBWorkerMessage] = {
    getCollaborationsCollection.map(collaborations =>
      collaborations.insert(document)
    ).map(_ => okMessage).recover(failStrategy)
  }

  override protected def delete(selector: BSONDocument, okMessage: DBWorkerMessage,
                      failStrategy: PartialFunction[Throwable, DBWorkerMessage]): Future[DBWorkerMessage] = {
    getCollaborationsCollection.map(collaborations =>
      collaborations.remove(selector)
    ).map(_ => okMessage).recover(failStrategy)
  }
}
