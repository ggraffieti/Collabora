package org.gammf.collabora.database.messages

import org.gammf.collabora.util._

trait QueryMessage

trait QueryNoteMessage extends QueryMessage {
  def note: SimpleNote
  def collaborationID: String
  def userID: String
}

case class InsertNoteMessage(note: SimpleNote, collaborationID: String, userID: String) extends QueryNoteMessage
case class UpdateNoteMessage(note: SimpleNote, collaborationID: String, userID: String) extends QueryNoteMessage
case class DeleteNoteMessage(note: SimpleNote, collaborationID: String, userID: String) extends QueryNoteMessage

trait QueryModuleMessage extends QueryMessage {
  def module: Module
  def collaborationID: String
  def userID: String
}

case class InsertModuleMessage(module: SimpleModule, collaborationID: String, userID: String) extends QueryModuleMessage
case class UpdateModuleMessage(module: SimpleModule, collaborationID: String, userID: String) extends QueryModuleMessage
case class DeleteModuleMessage(module: SimpleModule, collaborationID: String, userID: String) extends QueryModuleMessage

trait QueryCollaborationMessage extends QueryMessage {
  def collaboration: Collaboration
  def userID: String
}

case class InsertCollaborationMessage(collaboration: SimpleCollaboration, userID: String) extends QueryCollaborationMessage
case class UpdateCollaborationMessage(collaboration: SimpleCollaboration, userID: String) extends QueryCollaborationMessage
case class DeleteCollaborationMessage(collaboration: SimpleCollaboration, userID: String) extends QueryCollaborationMessage

trait QueryUserMessage extends QueryMessage {
  def user: CollaborationUser
  def collaborationID: String
  def userID: String
}

case class InsertUserMessage(user: CollaborationUser, collaborationID: String, userID: String) extends QueryUserMessage
case class UpdateUserMessage(user: CollaborationUser, collaborationID: String, userID: String) extends QueryUserMessage
case class DeleteUserMessage(user: CollaborationUser, collaborationID: String, userID: String) extends QueryUserMessage
