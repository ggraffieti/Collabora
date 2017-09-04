package org.gammf.collabora.authentication.messages

/**
  * Message sent by the server to the authenticationActor, to tell it to create the private collaboration for the user
  * @param username the username of the user already signed in
  */
case class CreatePrivateCollaborationMessage(username: String)
