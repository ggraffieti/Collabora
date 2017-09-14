package org.gammf.collabora.database.actors.master

import akka.actor.ActorRef
import akka.pattern.ask
import org.gammf.collabora.communication.messages.{CommunicationMessage, PublishCollaborationInCollaborationExchange, PublishErrorMessageInCollaborationExchange, PublishNotificationMessage}
import org.gammf.collabora.communication.actors.FirebaseActor
import org.gammf.collabora.communication.actors.rabbitmq.RabbitMQNotificationsSenderActor
import org.gammf.collabora.database.messages._
import org.gammf.collabora.util.{CollaborationMessage, ServerErrorCode, ServerErrorMessage, UpdateMessage, UpdateMessageTarget, UpdateMessageType}
import org.gammf.collabora.yellowpages.ActorService.ActorService
import org.gammf.collabora.yellowpages.util.Topic.ActorTopic
import org.gammf.collabora.yellowpages.util.Topic
import org.gammf.collabora.yellowpages.TopicElement._
import org.gammf.collabora.yellowpages.ActorService._

import scala.concurrent.ExecutionContext.Implicits.global

/**
  * The master actor that manages all the query about members.
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
        case insertMessage: InsertMemberMessage =>
          self ! SendInsertMemberNotificationMessage(insertMessage)
          self ! SendInsertMemberCollaborationMessage(insertMessage)

        case _ => getActorOrElse(Topic() :+ Communication :+ Notifications, Bridging, message)
          .foreach(_ ! buildNotificationMessage(query))
      }

      case _ => unhandled(_)
    }

    case message: SendInsertMemberNotificationMessage =>
      getActorOrElse(Topic() :+ Communication :+ Notifications, Bridging, message)
      .foreach(_ ! buildNotificationMessage(message.insertMessage))

    case message: SendInsertMemberCollaborationMessage =>
      getActorOrElse(Topic() :+ Database :+ Collaboration, Getter, message)
        .foreach(collaborationGetter =>
          (collaborationGetter ? GetCollaborationMessage(message.insertMessage.collaborationID))
            .mapTo[Option[List[org.gammf.collabora.util.Collaboration]]].map {
            case Some(head :: _) => getActorOrElse(Topic() :+ Communication :+ Collaborations :+ RabbitMQ , Master, message)
              .foreach(_ ! PublishCollaborationInCollaborationExchange(message.insertMessage.userID, CollaborationMessage(user=message.insertMessage.userID,collaboration = head)))
            case _ =>
          })

    case fail: QueryFailMessage => getActorOrElse(Topic() :+ Communication  :+ Collaborations :+ RabbitMQ, Master, fail)
      .foreach(_ ! PublishErrorMessageInCollaborationExchange(
        username = fail.username,
        message = ServerErrorMessage(user = fail.username, errorCode = ServerErrorCode.SERVER_ERROR)
      ))

  }: Receive) orElse super[AbstractDBMaster].receive

  private def buildNotificationMessage(query: QueryMemberMessage): CommunicationMessage = {
    PublishNotificationMessage(query.collaborationID, UpdateMessage(
      target = UpdateMessageTarget.MEMBER,
      messageType = getUpdateTypeFromQueryMessage(query),
      user = query.userID,
      member = Some(query.user),
      collaborationId = Some(query.collaborationID)))
  }
}

/**
  * Message used internally by a [[DBMasterMember]] in order to send a message to a [[RabbitMQNotificationsSenderActor]].
  *
  * @param insertMessage the message to be sent.
  */
private case class SendInsertMemberNotificationMessage(insertMessage: InsertMemberMessage)

/**
  * Message used internally by a [[DBMasterMember]] in order to send a message to a [[FirebaseActor]].
  * @param insertMessage the message to be sent.
  */
private case class SendInsertMemberCollaborationMessage(insertMessage: InsertMemberMessage)