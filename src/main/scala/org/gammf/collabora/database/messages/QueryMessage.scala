package org.gammf.collabora.database.messages

import org.gammf.collabora.util._

/**
  * A trait for query messages
  */
trait QueryMessage

/**
  * A trait for notes query. A message contains the note, the collaboration id and the user who have performed the update.
  */
trait QueryNoteMessage extends QueryMessage {
  def note: SimpleNote
  def collaborationID: String
  def userID: String
}

case class InsertNoteMessage(note: SimpleNote, collaborationID: String, userID: String) extends QueryNoteMessage
case class UpdateNoteMessage(note: SimpleNote, collaborationID: String, userID: String) extends QueryNoteMessage
case class DeleteNoteMessage(note: SimpleNote, collaborationID: String, userID: String) extends QueryNoteMessage

/**
  * A trait for module query. A message contains the module, the collaboration id and the user who have performed the update.
  */
trait QueryModuleMessage extends QueryMessage {
  def module: Module
  def collaborationID: String
  def userID: String
}

case class InsertModuleMessage(module: SimpleModule, collaborationID: String, userID: String) extends QueryModuleMessage
case class UpdateModuleMessage(module: SimpleModule, collaborationID: String, userID: String) extends QueryModuleMessage
case class DeleteModuleMessage(module: SimpleModule, collaborationID: String, userID: String) extends QueryModuleMessage

/**
  * A trait for collaboration query. A message contains the collaboration and the user who have performed the update.
  */
trait QueryCollaborationMessage extends QueryMessage {
  def collaboration: Collaboration
  def userID: String
}

case class InsertCollaborationMessage(collaboration: SimpleCollaboration, userID: String) extends QueryCollaborationMessage
case class UpdateCollaborationMessage(collaboration: SimpleCollaboration, userID: String) extends QueryCollaborationMessage
case class DeleteCollaborationMessage(collaboration: SimpleCollaboration, userID: String) extends QueryCollaborationMessage

/**
  * A trait for user query. A message contains the user, the collaboration id and the user who have performed the update.
  */
trait QueryUserMessage extends QueryMessage {
  def user: CollaborationUser
  def collaborationID: String
  def userID: String
}

case class InsertUserMessage(user: CollaborationUser, collaborationID: String, userID: String) extends QueryUserMessage
case class UpdateUserMessage(user: CollaborationUser, collaborationID: String, userID: String) extends QueryUserMessage
case class DeleteUserMessage(user: CollaborationUser, collaborationID: String, userID: String) extends QueryUserMessage
