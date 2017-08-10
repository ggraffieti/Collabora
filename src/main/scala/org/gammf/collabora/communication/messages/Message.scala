package org.gammf.collabora.communication.messages

/**
  * @author Manuel Peruzzi
  */

/**
  * Simple trait that represents a message between actors.
  */
trait Message

/**
  * Simple message used to start an actor.
  */
case class StartMessage() extends Message