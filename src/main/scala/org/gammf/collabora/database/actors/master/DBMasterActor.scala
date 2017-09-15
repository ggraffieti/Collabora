package org.gammf.collabora.database.actors.master

import akka.actor.ActorRef
import akka.pattern.{ask, pipe}
import akka.util.Timeout
import org.gammf.collabora.authentication.messages.{LoginMessage, SigninMessage, SigninResponseMessage}
import org.gammf.collabora.communication.messages.PublishErrorMessageInCollaborationExchange
import org.gammf.collabora.database.messages._
import org.gammf.collabora.util.{ServerErrorCode, ServerErrorMessage, UpdateMessage, UpdateMessageTarget}
import org.gammf.collabora.yellowpages.ActorService.ActorService
import org.gammf.collabora.yellowpages.util.Topic.ActorTopic

import scala.concurrent.ExecutionContext.Implicits.global
import scala.language.postfixOps
import scala.concurrent.duration._

import org.gammf.collabora.yellowpages.util.Topic
import org.gammf.collabora.yellowpages.TopicElement._
import org.gammf.collabora.yellowpages.ActorService._

/**
  * The actor that coordinate, create and act like a gateway for every request from and to the DB. It also create all the needed actors.
  */
class DBMasterActor(override val yellowPages: ActorRef, override val name: String,
                    override val topic: ActorTopic, override val service: ActorService) extends AbstractDBMaster {

  implicit val timeout: Timeout = Timeout(5 seconds)

  override def receive: Receive = ({
    case message: UpdateMessage => message.target match {
      case UpdateMessageTarget.NOTE => getActorOrElse(Topic() :+ Database :+ Note, Master, message).foreach(_ forward message)
      case UpdateMessageTarget.COLLABORATION => getActorOrElse(Topic() :+ Database :+ Collaboration, Master, message).foreach(_ forward message)
      case UpdateMessageTarget.MODULE => getActorOrElse(Topic() :+ Database :+ Module, Master, message).foreach(_ forward message)
      case UpdateMessageTarget.MEMBER => getActorOrElse(Topic() :+ Database :+ Member, Master, message).foreach(_ forward message)
    }

    case message: LoginMessage => getActorOrElse(Topic() :+ Database, Authenticator, message).foreach(_ forward message)

    case message: SigninMessage =>
      getActorOrElse(Topic() :+ Database, Authenticator, message).foreach(authenticationActor =>
      (authenticationActor ? message).mapTo[DBWorkerMessage].map(queryResponse =>
        SigninResponseMessage(queryResponse.isInstanceOf[QueryOkMessage])
      ) pipeTo sender)

    case message @ (_:GetAllCollaborationsMessage | _:GetCollaborationMessage) =>
      getActorOrElse(Topic() :+ Database :+ Collaboration, Master, message).foreach(_ forward message)

    case fail: QueryFailMessage => getActorOrElse(Topic() :+ Communication :+ Collaborations :+ RabbitMQ, Master, fail).
      foreach(_ ! PublishErrorMessageInCollaborationExchange(
        username = fail.username,
        message = ServerErrorMessage(user = fail.username, errorCode = ServerErrorCode.SERVER_ERROR)
      ))

    case _: NoActionMessage => unhandled(_)
  }: Receive) orElse super[AbstractDBMaster].receive
}
