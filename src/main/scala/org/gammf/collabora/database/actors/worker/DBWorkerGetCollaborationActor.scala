package org.gammf.collabora.database.actors.worker

import akka.actor.{ActorRef, Stash}
import akka.pattern.pipe
import org.gammf.collabora.database._
import org.gammf.collabora.database.messages._
import org.gammf.collabora.util.Collaboration
import org.gammf.collabora.yellowpages.ActorService.{ActorService, ConnectionHandler}
import org.gammf.collabora.yellowpages.messages.RegistrationResponseMessage
import reactivemongo.bson.BSONDocument

import scala.concurrent.ExecutionContext.Implicits.global

import org.gammf.collabora.yellowpages.util.Topic
import org.gammf.collabora.yellowpages.TopicElement._
import org.gammf.collabora.yellowpages.util.Topic.ActorTopic

/***
  * A worker that perform a get on collaboration collection and communicate with CollaborationMembersActor
  * @param connectionActor the actor that mantains the connection with the DB.
  * @param collaborationActor the actor that manage the exchange "collaborations"
  */
class DBWorkerGetCollaborationActor(override val yellowPages: ActorRef, override val name: String,
                                    override val topic: ActorTopic, override val service: ActorService)
  extends CollaborationsDBWorker[Option[List[Collaboration]]] with Stash {

  override def receive: Receive = {
    //TODO consider: these three methods in super class?
    case message: RegistrationResponseMessage => getActorOrElse(Topic() :+ Database, ConnectionHandler, message)
      .foreach(_ ! AskConnectionMessage())

    case message: GetConnectionMessage =>
      connection = Some(message.connection)
      unstashAll()

    case _ if connection.isEmpty => stash()

    case message: GetCollaborationMessage =>
      find(
        selector = BSONDocument(COLLABORATION_ID -> message.collaborationID),
        okStrategy = optionBson => optionBson.map(bsonDocument => List(bsonDocument.as[Collaboration])),
        failStrategy = { case _: Exception => None}
      ) pipeTo sender

    case message: GetAllCollaborationsMessage =>
      findAll(
        selector = BSONDocument(COLLABORATION_USERS + "." + COLLABORATION_USER_USERNAME -> message.username),
        okStrategy = bsonList => Some(bsonList.map(_.as[Collaboration])),
        failStrategy = { case _: Exception => None}
      ) pipeTo sender
  }
}
