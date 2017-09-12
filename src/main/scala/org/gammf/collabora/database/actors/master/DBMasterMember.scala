package org.gammf.collabora.database.actors.master

import akka.actor.ActorRef
import akka.pattern.ask
import org.gammf.collabora.communication.messages.{PublishErrorMessageInCollaborationExchange, PublishNotificationMessage}
import org.gammf.collabora.database.messages._
import org.gammf.collabora.util.{ServerErrorCode, ServerErrorMessage, UpdateMessage, UpdateMessageTarget, UpdateMessageType}
import org.gammf.collabora.yellowpages.ActorService.ActorService
import org.gammf.collabora.yellowpages.util.Topic.ActorTopic

import org.gammf.collabora.yellowpages.util.Topic
import org.gammf.collabora.yellowpages.TopicElement._
import org.gammf.collabora.yellowpages.ActorService._

import scala.concurrent.ExecutionContext.Implicits.global

/**
  * The master actor that manages all the query about members.
  * @param system the actor system, used for create the needed workers.
  * @param connectionManagerActor The system-unique [[org.gammf.collabora.database.actors.ConnectionManagerActor]], used for mantain a
  *                               connection with the database
  * @param notificationActor The actor used for notify the client that a query is went good.
  * @param getCollaborationActor The actor used for notify the member that it's just been added to a collaboration, and send him the collaboration
  * @param publishCollaborationExchangeActor the actor used for send notifications in the collaboration exchange
  */
class DBMasterMember(override val yellowPages: ActorRef, override val name: String,
                     override val topic: ActorTopic, override val service: ActorService) extends AbstractDBMaster {

  override def receive: Receive = ({

    case message: UpdateMessage => message.target match {
      case UpdateMessageTarget.MEMBER => message.messageType match {
        case UpdateMessageType.CREATION =>
          getActorOrElse(Topic() :+ Database :+ Member, ExistenceChecking, message).
            foreach(checkMemberWorker =>
          (checkMemberWorker ? IsMemberExistsMessage(message.member.get.user)).mapTo[QueryOkMessage].map(query => query.queryGoneWell match {
            case member: IsMemberExistsResponseMessage =>
              if (member.isRegistered)
                getActorOrElse(Topic() :+ Database :+ Member, DefaultWorker, message).foreach(_ ! InsertMemberMessage(message.member.get, message.collaborationId.get, message.user))
              else
                getActorOrElse(Topic() :+ Communication :+ Collaborations :+ RabbitMQ, Master, message).
                  foreach(_ ! PublishErrorMessageInCollaborationExchange(
                    username = message.user,
                    message = ServerErrorMessage(message.user, ServerErrorCode.MEMBER_NOT_FOUND)
                  ))
            case _ => unhandled(_)
          }))
        case UpdateMessageType.UPDATING => getActorOrElse(Topic() :+ Database :+ Member, DefaultWorker, message)
          .foreach(_ ! UpdateMemberMessage(message.member.get, message.collaborationId.get, message.user))
        case UpdateMessageType.DELETION => getActorOrElse(Topic() :+ Database :+ Member, DefaultWorker, message)
          .foreach(_ ! DeleteMemberMessage(message.member.get, message.collaborationId.get, message.user))}
    }

    case message: QueryOkMessage => message.queryGoneWell match {
      case query: QueryMemberMessage => query match {
        case _: InsertMemberMessage => //TODO; getCollaborationActor ! InsertMemberMessage(query.user, query.collaborationID, query.userID)
          getActorOrElse(Topic() :+ Communication :+ Notifications, Bridging, message)
            .foreach(_ ! PublishNotificationMessage(query.collaborationID, UpdateMessage(
              target = UpdateMessageTarget.MEMBER,
              messageType = getUpdateTypeFromQueryMessage(query),
              user = query.userID,
              member = Some(query.user),
              collaborationId = Some(query.collaborationID))))

        case _ => getActorOrElse(Topic() :+ Communication :+ Notifications, Bridging, message)
          .foreach(_ ! PublishNotificationMessage(query.collaborationID, UpdateMessage(
            target = UpdateMessageTarget.MEMBER,
            messageType = getUpdateTypeFromQueryMessage(query),
            user = query.userID,
            member = Some(query.user),
            collaborationId = Some(query.collaborationID))))
      }

      case _ => unhandled(_)
    }

    case fail: QueryFailMessage => getActorOrElse(Topic() :+ Communication  :+ Collaborations :+ RabbitMQ, Master, fail)
      .foreach(_ ! PublishErrorMessageInCollaborationExchange(
        username = fail.username,
        message = ServerErrorMessage(user = fail.username, errorCode = ServerErrorCode.SERVER_ERROR)
      ))

  }: Receive) orElse super[AbstractDBMaster].receive
}
