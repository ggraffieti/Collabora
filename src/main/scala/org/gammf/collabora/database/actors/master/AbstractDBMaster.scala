package org.gammf.collabora.database.actors.master

import akka.actor.Actor
import org.gammf.collabora.database.messages._
import org.gammf.collabora.util.UpdateMessageType
import org.gammf.collabora.util.UpdateMessageType.UpdateMessageType

abstract class AbstractDBMaster extends Actor {

  protected def getUpdateTypeFromQueryMessage(query: QueryMessage): UpdateMessageType = query match {
    case _: InsertNoteMessage | _: InsertCollaborationMessage | _: InsertModuleMessage | _: InsertMemberMessage => UpdateMessageType.CREATION
    case _: UpdateNoteMessage | _: UpdateCollaborationMessage | _: UpdateModuleMessage | _: UpdateMemberMessage => UpdateMessageType.UPDATING
    case _: DeleteNoteMessage | _: DeleteCollaborationMessage | _: DeleteModuleMessage | _: DeleteMemberMessage => UpdateMessageType.DELETION
  }
}
