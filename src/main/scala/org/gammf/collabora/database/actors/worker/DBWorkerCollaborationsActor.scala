package org.gammf.collabora.database.actors.worker

import akka.actor.{ActorRef, Stash}
import akka.pattern.pipe
import org.gammf.collabora.database._
import org.gammf.collabora.database.messages._
import org.gammf.collabora.util.Collaboration
import reactivemongo.api.Cursor
import reactivemongo.bson.{BSON, BSONDocument, BSONObjectID}

import scala.concurrent.ExecutionContext.Implicits.global
import org.gammf.collabora.yellowpages.util.Topic
import org.gammf.collabora.yellowpages.TopicElement._
import org.gammf.collabora.yellowpages.ActorService._
import org.gammf.collabora.yellowpages.messages.RegistrationResponseMessage
import org.gammf.collabora.yellowpages.util.Topic.ActorTopic

/**
  * A worker that performs query on collaborations.
  * @param connectionActor the actor that mantains the connection with the DB.
  */
class DBWorkerCollaborationsActor(override val yellowPages: ActorRef, override val name: String,
                                  override val topic: ActorTopic, override val service: ActorService)
  extends CollaborationsDBWorker[DBWorkerMessage] with DefaultDBWorker with Stash {

  override def receive: Receive = ({
    //TODO consider: these three methods in super class?
    case message: RegistrationResponseMessage => getActorOrElse(Topic() :+ Database, ConnectionHandler, message)
      .foreach(_ ! AskConnectionMessage())

    case message: GetConnectionMessage =>
      connection = Some(message.connection)
      unstashAll()

    case _ if connection.isEmpty => stash()

    case message: InsertCollaborationMessage =>
      val bsonCollaboration: BSONDocument = BSON.write(message.collaboration) // necessary conversion, sets the collaborationID
      insert(
        document = bsonCollaboration,
        okMessage = QueryOkMessage(InsertCollaborationMessage(bsonCollaboration.as[Collaboration], message.userID)),
        failStrategy = defaultDBWorkerFailStrategy(message.userID)
      ) pipeTo sender

    case message: UpdateCollaborationMessage =>
      update(
        selector = BSONDocument(COLLABORATION_ID -> BSONObjectID.parse(message.collaboration.id.get).get),
        query = BSONDocument("$set" -> BSONDocument(COLLABORATION_NAME -> message.collaboration.name)),
        okMessage = QueryOkMessage(message),
        failStrategy = defaultDBWorkerFailStrategy(message.userID)
      ) pipeTo sender

    case message: DeleteCollaborationMessage =>
      delete(
        selector = BSONDocument(COLLABORATION_ID -> BSONObjectID.parse(message.collaboration.id.get).get),
        okMessage = QueryOkMessage(message),
        failStrategy = defaultDBWorkerFailStrategy(message.userID)
      ) pipeTo sender

      //TODO is this necessary here?
    case message: GetAllCollaborationsMessage =>
      getCollaborationsCollection.map(collaborations =>
        collaborations.find(BSONDocument(
          COLLABORATION_USERS + "." + COLLABORATION_USER_USERNAME -> message.username
        )).cursor[BSONDocument]().collect[List](-1, Cursor.FailOnError[List[BSONDocument]]())) // -1 is no limit list
        .flatten.map(list =>
        AllCollaborationsMessage(list.map(bson => bson.as[Collaboration]))) pipeTo sender

  }: Receive) orElse super[CollaborationsDBWorker].receive
}