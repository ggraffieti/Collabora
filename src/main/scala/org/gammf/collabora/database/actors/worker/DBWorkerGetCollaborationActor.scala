package org.gammf.collabora.database.actors.worker

import akka.actor.{ActorRef, Props, Stash}
import akka.pattern.pipe
import org.gammf.collabora.database._
import org.gammf.collabora.database.messages._
import org.gammf.collabora.util.Collaboration
import reactivemongo.bson.BSONObjectID

import org.gammf.collabora.yellowpages.ActorService.{ActorService, Getter}
import reactivemongo.bson.BSONDocument

import scala.concurrent.ExecutionContext.Implicits.global
import org.gammf.collabora.yellowpages.util.Topic.ActorTopic

/***
  * A worker that perform a get on collaboration collection and communicate with CollaborationMembersActor
  */
class DBWorkerGetCollaborationActor(override val yellowPages: ActorRef,
                                    override val name: String,
                                    override val topic: ActorTopic,
                                    override val service: ActorService = Getter)
  extends CollaborationsDBWorker[Option[List[Collaboration]]] with Stash {

  override def receive: Receive = super.receive orElse ({

    case message: GetCollaborationMessage =>
      find(
        selector = BSONDocument(COLLABORATION_ID -> BSONObjectID.parse(message.collaborationID).get),
        okStrategy = optionBson => optionBson.map(bsonDocument => List(bsonDocument.as[Collaboration])),
        failStrategy = { case _: Exception => None}
      ) pipeTo sender

    case message: GetAllCollaborationsMessage =>
      findAll(
        selector = BSONDocument(COLLABORATION_USERS + "." + COLLABORATION_USER_USERNAME -> message.username),
        okStrategy = bsonList => Some(bsonList.map(_.as[Collaboration])),
        failStrategy = { case _: Exception => None}
      ) pipeTo sender
  }: Receive )
}