package org.gammf.collabora.communication.messages

import org.gammf.collabora.util.{Collaboration, CollaborationMessage, ServerErrorMessage, UpdateMessage}
import org.gammf.collabora.communication.actors.NotificationsDispatcherActor
import org.gammf.collabora.communication.actors.NotificationsSenderActor
import org.gammf.collabora.communication.actors.FirebaseActor

/**
  * Simple trait that represent a message about the communication.
  */
sealed trait CommunicationMessage extends Message

/**
  * Contains the update message sent by a client.
  * @param text the text of the update.
  */
case class ClientUpdateMessage(text: String) extends CommunicationMessage

/**
  * Represents a notification message to be published.
  * @param collaborationID the identifier of the collaboration to which the message is addressed.
  * @param message the text of the message to be published in json format.
  */
case class PublishNotificationMessage(collaborationID: String, message: UpdateMessage)
  extends CommunicationMessage

/**
  * Represents a message sent to a user that has just been added to a collaboration.
  * @param username the identifier of the user to which the message is addressed.
  * @param message the message to be published.
  */
case class PublishCollaborationInCollaborationExchange(username: String, message: CollaborationMessage)
  extends CommunicationMessage

/**
  * Represents a message sent to a user, for notify an error occured in the server.
  * @param username the identifier of the user to which the message is addressed.
  * @param message the error message to be published.
  */
case class PublishErrorMessageInCollaborationExchange(username: String, message: ServerErrorMessage)
  extends CommunicationMessage


case class PublishFirebaseNotification(collaborationID: String, collaboration: Collaboration)
  extends CommunicationMessage

/**
  * Message used internally by a [[NotificationsDispatcherActor]] in order to manage message-forwarding to a [[NotificationsSenderActor]]
 *
  * @param message the message to be forwarded
  */
case class ForwardMessageToRabbitMQNotificationActor(message: PublishNotificationMessage) extends CommunicationMessage

/**
  * Message used internally by a [[NotificationsDispatcherActor]] in order to manage message-forwarding to a [[FirebaseActor]]
 *
  * @param message the message to be forwarded
  */
case class ForwardMessageToFirebaseNotificationActor(message: PublishNotificationMessage) extends CommunicationMessage