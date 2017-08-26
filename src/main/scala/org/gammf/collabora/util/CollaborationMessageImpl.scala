package org.gammf.collabora.util

/***
  * Simple implementation of a collaboration message
  * @param user the user that require the creation of a collaboration or the join of a member in it
  * @param collaboration the collaboration affected, used for containing the collaboration id
  */
case class CollaborationMessageImpl(user: String, collaboration: Collaboration) extends CollaborationMessage {
}
