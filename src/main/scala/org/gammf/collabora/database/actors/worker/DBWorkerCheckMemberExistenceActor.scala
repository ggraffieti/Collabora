package org.gammf.collabora.database.actors.worker

import akka.actor.{ActorRef, Props, Stash}
import akka.pattern.pipe
import org.gammf.collabora.database.messages._
import reactivemongo.bson.BSONDocument
import org.gammf.collabora.database._

import scala.concurrent.ExecutionContext.Implicits.global
import org.gammf.collabora.yellowpages.ActorService._
import org.gammf.collabora.yellowpages.util.Topic.ActorTopic

class DBWorkerCheckMemberExistenceActor(override val yellowPages: ActorRef,
                                        override val name: String,
                                        override val topic: ActorTopic,
                                        override val service: ActorService = ExistenceChecking)
  extends UsersDBWorker[DBWorkerMessage] with DefaultDBWorker with Stash {

  override def receive: Receive = super.receive orElse ({

    case message: IsMemberExistsMessage =>
      find(
        selector = BSONDocument(USER_ID -> message.username),
        okStrategy = bsonDocumet => QueryOkMessage(IsMemberExistsResponseMessage(message.username, bsonDocumet.isDefined)),
        failStrategy = defaultDBWorkerFailStrategy(message.username)
      ) pipeTo sender
  }: Receive)
}

object DBWorkerCheckMemberExistenceActor {

  /**
    * Factory methods that return a [[Props]] to create a database worker check member existence registered actor
    * @param yellowPages the reference to the yellow pages root actor.
    * @param topic the topic to which this actor is going to be registered.
    * @return the [[Props]] to use to create a database worker check member existence actor.
    */
  def dbWorkerCheckMemberExistenceProps(yellowPages: ActorRef, topic: ActorTopic, name: String = "DBWorkerCheckMember") : Props =
    Props(new DBWorkerCheckMemberExistenceActor(yellowPages = yellowPages, name = name, topic = topic))
}
