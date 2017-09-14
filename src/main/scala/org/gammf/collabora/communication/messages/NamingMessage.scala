package org.gammf.collabora.communication.messages

import org.gammf.collabora.communication.CommunicationType

/**
  * Simple trait that represents a message about a rabbitMQ naming issue.
  */
sealed trait NamingMessage extends Message

/**
  * Represents a request for retrieve the rabbitMQ terminology names about a certain communication.
  * @param communicationType the type of the communication.
  */
case class ChannelNamesRequestMessage(communicationType: CommunicationType.Value)
  extends NamingMessage

/**
  * Represents a response to a rabbitMQ terminology names request.
  * @param exchange the name of the exchange to be used.
  * @param queue the name of the queue, only in case of subscribing channel.
  */
case class ChannelNamesResponseMessage(exchange: String, queue: Option[String])
  extends NamingMessage

