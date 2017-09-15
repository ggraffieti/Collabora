package org.gammf.collabora.database.actors.worker

import akka.actor.{ActorRef, Props, Stash}
import akka.pattern.pipe
import org.gammf.collabora.database._
import org.gammf.collabora.database.messages._
import org.gammf.collabora.yellowpages.ActorService.{ActorService, ConnectionHandler, DefaultWorker}
import reactivemongo.bson.{BSONDocument, BSONObjectID}

import scala.concurrent.ExecutionContext.Implicits.global
import org.gammf.collabora.yellowpages.util.Topic
import org.gammf.collabora.yellowpages.TopicElement._
import org.gammf.collabora.yellowpages.messages.RegistrationResponseMessage
import org.gammf.collabora.yellowpages.util.Topic.ActorTopic

/**
  * A worker that performs query on members.
  */
class DBWorkerMemberActor(override val yellowPages: ActorRef,
                          override val name: String,
                          override val topic: ActorTopic,
                          override val service: ActorService = DefaultWorker)
  extends CollaborationsDBWorker[DBWorkerMessage] with DefaultDBWorker with Stash {

  override def receive: Receive = ({
    //TODO consider: these three methods in super class?
    case message: RegistrationResponseMessage => getActorOrElse(Topic() :+ Database, ConnectionHandler, message)
      .foreach(_ ! AskConnectionMessage())

    case message: GetConnectionMessage =>
      connection = Some(message.connection)
      unstashAll()

    case _ if connection.isEmpty => stash()

    case message: InsertMemberMessage =>
      update(
        selector = BSONDocument(COLLABORATION_ID -> BSONObjectID.parse(message.collaborationID).get),
        query = BSONDocument("$push" -> BSONDocument(COLLABORATION_USERS -> message.user)),
        okMessage = QueryOkMessage(message),
        failStrategy = defaultDBWorkerFailStrategy(message.userID)
      ) pipeTo sender

    case message: UpdateMemberMessage =>
      update(
        selector = BSONDocument(
          COLLABORATION_ID -> BSONObjectID.parse(message.collaborationID).get,
          COLLABORATION_USERS + "." + COLLABORATION_USER_USERNAME -> message.user.user
        ),
        query = BSONDocument("$set" -> BSONDocument(COLLABORATION_USERS + ".$" -> message.user)),
        okMessage = QueryOkMessage(message),
        failStrategy = defaultDBWorkerFailStrategy(message.userID)
      ) pipeTo sender

    case message: DeleteMemberMessage =>
      update(
        selector = BSONDocument(COLLABORATION_ID -> BSONObjectID.parse(message.collaborationID).get),
        query = BSONDocument("$pull" -> BSONDocument(COLLABORATION_USERS -> BSONDocument(COLLABORATION_USER_USERNAME -> message.user.user))),
        okMessage = QueryOkMessage(message),
        failStrategy = defaultDBWorkerFailStrategy(message.userID)
      ) pipeTo sender

  }: Receive) orElse super[CollaborationsDBWorker].receive
}

object DBWorkerMemberActor {
  /**
    * Factory methods that return a [[Props]] to create a database worker member registered actor
    * @param yellowPages the reference to the yellow pages root actor.
    * @param topic the topic to which this actor is going to be registered.
    * @return the [[Props]] to use to create a database worker member actor.
    */
  def dbWorkerMemberProps(yellowPages: ActorRef, topic: ActorTopic, name: String = "DBWorkerMember") : Props =
    Props(new DBWorkerMemberActor(yellowPages = yellowPages, name = name, topic = topic))
}