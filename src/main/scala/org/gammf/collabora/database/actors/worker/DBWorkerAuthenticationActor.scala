package org.gammf.collabora.database.actors.worker

import akka.actor.{ActorRef, Stash}
import akka.pattern.pipe
import org.gammf.collabora.authentication.messages.{LoginMessage, SigninMessage}
import org.gammf.collabora.database.messages._
import org.gammf.collabora.util.User
import reactivemongo.bson.{BSON, BSONDocument}
import org.gammf.collabora.database._

import scala.concurrent.ExecutionContext.Implicits.global
import org.gammf.collabora.yellowpages.util.Topic
import org.gammf.collabora.yellowpages.TopicElement._
import org.gammf.collabora.yellowpages.ActorService._
import org.gammf.collabora.yellowpages.messages.RegistrationResponseMessage
import org.gammf.collabora.yellowpages.util.Topic.ActorTopic

/**
  * A [[DBWorker]] used for authentication purpose. It manages [[LoginMessage]] and [[SigninMessage]].
  */
class DBWorkerAuthenticationActor(override val yellowPages: ActorRef, override val name: String,
                                  override val topic: ActorTopic, override val service: ActorService)
  extends UsersDBWorker[DBWorkerMessage] with DefaultDBWorker with Stash {

  override def receive: Receive = super.receive orElse ({

    case message: LoginMessage =>
      find(
        selector = BSONDocument(USER_ID -> message.username),
        okStrategy = bsonDocument =>  {
          if (bsonDocument.isDefined) AuthenticationMessage(Some(bsonDocument.get.as[User]))
          else AuthenticationMessage(None)
        },
        failStrategy = defaultDBWorkerFailStrategy(message.username)
      ) pipeTo sender

    case message: SigninMessage =>
      insert(
        document = BSON.write(message.user),
        okMessage = QueryOkMessage(InsertUserMessage(message.user)),
        failStrategy = defaultDBWorkerFailStrategy(message.user.username)
      ) pipeTo sender

    case _ => unhandled(_)
  }: Receive)
}
