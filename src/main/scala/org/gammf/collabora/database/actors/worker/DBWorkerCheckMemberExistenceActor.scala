package org.gammf.collabora.database.actors.worker

import akka.actor.{ActorRef, Stash}
import akka.pattern.pipe
import org.gammf.collabora.database.messages._
import reactivemongo.bson.BSONDocument
import org.gammf.collabora.database._

import scala.concurrent.ExecutionContext.Implicits.global
import org.gammf.collabora.yellowpages.util.Topic
import org.gammf.collabora.yellowpages.TopicElement._
import org.gammf.collabora.yellowpages.ActorService._
import org.gammf.collabora.yellowpages.messages.RegistrationResponseMessage
import org.gammf.collabora.yellowpages.util.Topic.ActorTopic

class DBWorkerCheckMemberExistenceActor(override val yellowPages: ActorRef, override val name: String,
                                        override val topic: ActorTopic, override val service: ActorService)
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
