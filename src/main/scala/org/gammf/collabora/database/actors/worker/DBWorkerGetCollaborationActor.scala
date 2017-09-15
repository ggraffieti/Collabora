package org.gammf.collabora.database.actors.worker

import akka.actor.{ActorRef, Props, Stash}
import akka.pattern.pipe
import org.gammf.collabora.database._
import org.gammf.collabora.database.messages._
import org.gammf.collabora.util.Collaboration
import org.gammf.collabora.yellowpages.ActorService.{ActorService, ConnectionHandler, Getter}
import org.gammf.collabora.yellowpages.messages.RegistrationResponseMessage
import reactivemongo.bson.BSONDocument

import scala.concurrent.ExecutionContext.Implicits.global
import org.gammf.collabora.yellowpages.util.Topic
import org.gammf.collabora.yellowpages.TopicElement._
import org.gammf.collabora.yellowpages.util.Topic.ActorTopic

/***
  * A worker that perform a get on collaboration collection and communicate with CollaborationMembersActor
  */
class DBWorkerGetCollaborationActor(override val yellowPages: ActorRef,
                                    override val name: String,
                                    override val topic: ActorTopic,
                                    override val service: ActorService = Getter)
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

object DBWorkerGetCollaborationActor {

  /**
    * Factory methods that return a [[Props]] to create a database worker get collaboration registered actor
    * @param yellowPages the reference to the yellow pages root actor.
    * @param topic the topic to which this actor is going to be registered.
    * @return the [[Props]] to use to create a database worker get collaboration actor.
    */
  def dbWorkerGetCollaborationProps(yellowPages: ActorRef, topic: ActorTopic, name: String = "DBWorkerGetCollaboration") : Props =
    Props(new DBWorkerGetCollaborationActor(yellowPages = yellowPages, name = name, topic = topic))
}
