package org.gammf.collabora.authentication.messages

/**
  * Used by the [[org.gammf.collabora.authentication.actors.AuthenticationActor]] to ask at the DB master, to send all the
  * collaborations at the given user. This message is sent after the successful login by the user.
  * @param username the user.
  */
case class SendAllCollaborationsMessage(username: String)
