package org.gammf.collabora.database.messages

/**
  * A message that tell the receiver to not do no further action.
  * If any actor receve this message, the action have to be unhandled(message).
  */
case class NoActionMessage()
