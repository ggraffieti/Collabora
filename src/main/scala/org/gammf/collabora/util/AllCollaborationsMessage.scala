package org.gammf.collabora.util

trait AllCollaborationsMessage {

  def collaborationList: List[Collaboration]

}

/**
  * Message used to respond to a [[org.gammf.collabora.database.messages.GetAllCollaborationsMessage]]
  * giving a list of all the collaborations of the requested user
  */
object AllCollaborationsMessage {

  def apply(collaborationList: List[Collaboration]): AllCollaborationsMessage = SimpleAllCollaborationsMessage(collaborationList)

  def unapply(arg: AllCollaborationsMessage): Option[List[Collaboration]] = Some(arg.collaborationList)

}

case class SimpleAllCollaborationsMessage(collaborationList: List[Collaboration]) extends AllCollaborationsMessage
