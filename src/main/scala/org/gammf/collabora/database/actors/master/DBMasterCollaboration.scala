package org.gammf.collabora.database.actors.master

import akka.actor.{ActorRef, Props}
import org.gammf.collabora.communication.messages.{PublishCollaborationInCollaborationExchange, PublishErrorMessageInCollaborationExchange, PublishNotificationMessage}
import org.gammf.collabora.database.messages._
import org.gammf.collabora.util.{CollaborationMessage, CollaborationType, ServerErrorCode, ServerErrorMessage, UpdateMessage, UpdateMessageTarget, UpdateMessageType}
import org.gammf.collabora.yellowpages.ActorService.ActorService
import org.gammf.collabora.yellowpages.util.Topic.ActorTopic
import org.gammf.collabora.yellowpages.util.Topic
import org.gammf.collabora.yellowpages.TopicElement._
import org.gammf.collabora.yellowpages.ActorService._

/**
  * The master actor that manages all the query about collaborations.
  */
class DBMasterCollaboration(override val yellowPages: ActorRef,
                            override val name: String,
                            override val topic: ActorTopic,
                            override val service: ActorService = Master) extends AbstractDBMaster {

  override def receive: Receive = ({

    case message: UpdateMessage => message.target match {
      case UpdateMessageTarget.COLLABORATION => message.messageType match {
        case UpdateMessageType.CREATION =>
          if (message.collaboration.get.collaborationType == CollaborationType.PRIVATE)
            getActorOrElse(Topic() :+ Database :+ Collaboration, DefaultWorker, message).foreach(_ forward InsertCollaborationMessage(message.collaboration.get, message.user))
          else
            getActorOrElse(Topic() :+ Database :+ Collaboration, DefaultWorker, message).foreach(_ ! InsertCollaborationMessage(message.collaboration.get, message.user))
        case UpdateMessageType.UPDATING => getActorOrElse(Topic() :+ Database :+ Collaboration, DefaultWorker, message).foreach(_ ! UpdateCollaborationMessage(message.collaboration.get, message.user))
        case UpdateMessageType.DELETION => getActorOrElse(Topic() :+ Database :+ Collaboration, DefaultWorker, message).foreach(_ ! DeleteCollaborationMessage(message.collaboration.get, message.user))
      }
    }

    case message @ (_:GetAllCollaborationsMessage | _:GetCollaborationMessage) =>
      getActorOrElse(Topic() :+ Database :+ Collaboration, Getter, message).foreach(_ forward message)

    case message: QueryOkMessage => message.queryGoneWell match {
      case query: QueryCollaborationMessage => query match {
        case _: InsertCollaborationMessage =>
          getActorOrElse(Topic() :+ Communication :+ Collaborations :+ RabbitMQ , Master, message)
            .foreach(_ ! PublishCollaborationInCollaborationExchange(query.userID, CollaborationMessage(user=query.userID,collaboration = query.collaboration)))
        case _ =>
          getActorOrElse(Topic() :+ Communication :+ Notifications, Bridging, message)
            .foreach(_ ! PublishNotificationMessage(query.collaboration.id.get, UpdateMessage(
              target = UpdateMessageTarget.COLLABORATION,
              messageType = getUpdateTypeFromQueryMessage(query),
              user = query.userID,
              collaboration = Some(query.collaboration),
              collaborationId = Some(query.collaboration.id.get))))
      }
      case _ => unhandled(_)
    }

    case fail: QueryFailMessage => getActorOrElse(Topic() :+ Communication :+ Collaborations :+ RabbitMQ, Master, fail)
      .foreach(_ ! PublishErrorMessageInCollaborationExchange(
        username = fail.username,
        message = ServerErrorMessage(user = fail.username, errorCode = ServerErrorCode.SERVER_ERROR)
      ))

  }: Receive) orElse super[AbstractDBMaster].receive
}

object DBMasterCollaboration {
  /**
    * Factory method that returns a Props to create an already-registered database master collaboration actor.
    * @param yellowPages the reference to the yellow pages root actor.
    * @param topic the topic to which this actor is going to be registered.
    * @return the Props to use to create a database master collaboration actor.
    */

  def dbMasterCollaborationProps(yellowPages: ActorRef, topic: ActorTopic, name: String = "DBMasterCollaboration") : Props =
    Props(new DBMasterCollaboration(yellowPages = yellowPages, name = name, topic = topic))
}
