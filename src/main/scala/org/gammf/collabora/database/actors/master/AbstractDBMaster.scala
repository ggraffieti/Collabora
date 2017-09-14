package org.gammf.collabora.database.actors.master

import org.gammf.collabora.database.messages._
import org.gammf.collabora.util.UpdateMessageType
import org.gammf.collabora.util.UpdateMessageType.UpdateMessageType
import org.gammf.collabora.yellowpages.actors.BasicActor

/**
  * An abstract implementation of a DBMaster actor. It contains utility methods, used in every DBMaster.
  */
abstract class AbstractDBMaster extends BasicActor {

  override def receive: Receive = super.receive

  protected def getUpdateTypeFromQueryMessage(query: QueryMessage): UpdateMessageType = query match {
    case _: InsertNoteMessage | _: InsertCollaborationMessage | _: InsertModuleMessage | _: InsertMemberMessage => UpdateMessageType.CREATION
    case _: UpdateNoteMessage | _: UpdateCollaborationMessage | _: UpdateModuleMessage | _: UpdateMemberMessage => UpdateMessageType.UPDATING
    case _: DeleteNoteMessage | _: DeleteCollaborationMessage | _: DeleteModuleMessage | _: DeleteMemberMessage => UpdateMessageType.DELETION
  }
}
