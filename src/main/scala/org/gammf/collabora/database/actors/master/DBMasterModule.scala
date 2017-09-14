package org.gammf.collabora.database.actors.master

import akka.actor.ActorRef
import org.gammf.collabora.communication.messages.{PublishErrorMessageInCollaborationExchange, PublishNotificationMessage}
import org.gammf.collabora.database.messages._
import org.gammf.collabora.util.{ServerErrorCode, ServerErrorMessage, UpdateMessage, UpdateMessageTarget, UpdateMessageType}
import org.gammf.collabora.yellowpages.ActorService.ActorService
import org.gammf.collabora.yellowpages.util.Topic.ActorTopic

import org.gammf.collabora.yellowpages.util.Topic
import org.gammf.collabora.yellowpages.TopicElement._
import org.gammf.collabora.yellowpages.ActorService._

/**
  * The master actor that manages all the query about modules.
  */
class DBMasterModule(override val yellowPages: ActorRef, override val name: String,
                     override val topic: ActorTopic, override val service: ActorService) extends AbstractDBMaster {

  override def receive: Receive = ({

    case message: UpdateMessage => message.target match {
      case UpdateMessageTarget.MODULE => getActorOrElse(Topic() :+ Database :+ Module, DefaultWorker, message)
        .foreach(_ ! (message.messageType match {
          case UpdateMessageType.CREATION => InsertModuleMessage(message.module.get, message.collaborationId.get, message.user)
          case UpdateMessageType.UPDATING => UpdateModuleMessage(message.module.get, message.collaborationId.get, message.user)
          case UpdateMessageType.DELETION => DeleteModuleMessage(message.module.get, message.collaborationId.get, message.user)
        }))
    }

    case message: QueryOkMessage => message.queryGoneWell match {
      case query: QueryModuleMessage =>
        getActorOrElse(Topic() :+ Communication :+ Notifications, Bridging, message)
          .foreach(_ ! PublishNotificationMessage(query.collaborationID, UpdateMessage(
            target = UpdateMessageTarget.MODULE,
            messageType = getUpdateTypeFromQueryMessage(query),
            user = query.userID,
            module = Some(query.module),
            collaborationId = Some(query.collaborationID))))
      case _ => unhandled(_)
    }

    case fail: QueryFailMessage => getActorOrElse(Topic() :+ Communication :+ Collaborations :+ RabbitMQ, Master, fail)
      .foreach(_ ! PublishErrorMessageInCollaborationExchange(
        username = fail.username,
        message = ServerErrorMessage(user = fail.username, errorCode = ServerErrorCode.SERVER_ERROR)
      ))

  }: Receive) orElse super[AbstractDBMaster].receive
}
