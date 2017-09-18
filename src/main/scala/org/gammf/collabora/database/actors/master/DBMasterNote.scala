package org.gammf.collabora.database.actors.master

import akka.actor.{ActorRef, Props}
import org.gammf.collabora.communication.messages.{PublishErrorMessageInCollaborationExchange, PublishNotificationMessage}
import org.gammf.collabora.database.messages._
import org.gammf.collabora.util.{ServerErrorCode, ServerErrorMessage, UpdateMessage, UpdateMessageTarget, UpdateMessageType}
import org.gammf.collabora.yellowpages.util.Topic
import org.gammf.collabora.yellowpages.TopicElement._
import org.gammf.collabora.yellowpages.ActorService._
import org.gammf.collabora.yellowpages.util.Topic.ActorTopic

/**
  * The master actor that manages all the query about notes.
  */
class DBMasterNote(override val yellowPages: ActorRef,
                   override val name: String,
                   override val topic: ActorTopic,
                   override val service: ActorService = Master) extends AbstractDBMaster {

  override def receive: Receive = ({

    case message: UpdateMessage => message.target match {
      case UpdateMessageTarget.NOTE => getActorOrElse(Topic() :+ Database :+ Note, DefaultWorker, message)
        .foreach(_ !  (message.messageType match {
        case UpdateMessageType.CREATION => InsertNoteMessage(message.note.get, message.collaborationId.get, message.user)
        case UpdateMessageType.UPDATING => UpdateNoteMessage(message.note.get, message.collaborationId.get, message.user)
        case UpdateMessageType.DELETION => DeleteNoteMessage(message.note.get, message.collaborationId.get, message.user)
      }))
    }

    case message: QueryOkMessage => message.queryGoneWell match {
      case query: QueryNoteMessage => getActorOrElse(Topic() :+ Communication :+ Notifications, Bridging, message)
        .foreach(_ ! PublishNotificationMessage(query.collaborationID, UpdateMessage(
          target = UpdateMessageTarget.NOTE,
          messageType = getUpdateTypeFromQueryMessage(query),
          user = query.userID,
          note = Some(query.note),
          collaborationId = Some(query.collaborationID))))
        if (query.note.module.isDefined) getActorOrElse(Topic() :+ Database :+ Module, StateChanger, message)
          .foreach(_ ! ChangeModuleState(query.collaborationID, query.note.module.get))
      case _ => unhandled(_)
    }

    case fail: QueryFailMessage => getActorOrElse(Topic() :+ Communication :+ Collaborations :+ RabbitMQ, Master, fail)
      .foreach(_ ! PublishErrorMessageInCollaborationExchange(
        username = fail.username,
        message = ServerErrorMessage(user = fail.username, errorCode = ServerErrorCode.SERVER_ERROR)
      ))

  }: Receive) orElse super[AbstractDBMaster].receive
}

object DBMasterNote {
  /**
    * Factory method that returns a Props to create an already-registered database master note actor.
    * @param yellowPages the reference to the yellow pages root actor.
    * @param topic the topic to which this actor is going to be registered.
    * @return the Props to use to create a database master note actor.
    */

  def dbMasterNoteProps(yellowPages: ActorRef, topic: ActorTopic, name: String = "DBMasterNote") : Props =
    Props(new DBMasterNote(yellowPages = yellowPages, name = name, topic = topic))
}
