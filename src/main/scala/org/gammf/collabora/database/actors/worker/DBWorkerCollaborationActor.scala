package org.gammf.collabora.database.actors.worker

import akka.actor.{ActorRef, Props}
import akka.pattern.pipe
import org.gammf.collabora.database._
import org.gammf.collabora.database.messages._
import org.gammf.collabora.util.Collaboration
import reactivemongo.bson.{BSON, BSONDocument, BSONObjectID}

import scala.concurrent.ExecutionContext.Implicits.global
import org.gammf.collabora.yellowpages.ActorService._
import org.gammf.collabora.yellowpages.util.Topic.ActorTopic

/**
  * A worker that performs query on collaborations.
  */
class DBWorkerCollaborationActor(override val yellowPages: ActorRef, override val name: String,
                                 override val topic: ActorTopic, override val service: ActorService = DefaultWorker)
  extends CollaborationsDBWorker[DBWorkerMessage] with DefaultDBWorker {

  override def receive: Receive =  super.receive orElse ({

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

    case _ => println("["+ name + "] Huh?"); unhandled(_)

  }: Receive)
}

object DBWorkerCollaborationActor {
  /**
    * Factory method that returns a Props to create an already-registered database worker collaborations actor.
    * @param yellowPages the reference to the yellow pages root actor.
    * @param topic the topic to which this actor is going to be registered.
    * @return the Props to use to create a database worker collaborations actor.
    */

  def dbWorkerCollaborationProps(yellowPages: ActorRef, topic: ActorTopic, name: String = "DBWorkerCollaborations") : Props =
    Props(new DBWorkerCollaborationActor(yellowPages = yellowPages, name = name, topic = topic))
}