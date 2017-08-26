package org.gammf.collabora.communication.messages

import org.gammf.collabora.util.{CollaborationMessage, UpdateMessage}
import play.api.libs.json.JsValue

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
  * @param message the text of the message to be published in json format.
  */
case class PublishMemberAddedMessage(username: String, message: CollaborationMessage)
  extends CommunicationMessage

