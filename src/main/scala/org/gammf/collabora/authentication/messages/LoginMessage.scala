package org.gammf.collabora.authentication.messages

/**
  * A message that is sent by the server to its authentication actor, in order to check if the given user
  * is registered on the server.
  * @param username the username of the user that ask for a login.
  */
case class LoginMessage(username: String)
