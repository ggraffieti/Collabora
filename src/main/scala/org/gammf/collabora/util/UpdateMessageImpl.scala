package org.gammf.collabora.util

import org.gammf.collabora.util.UpdateMessageTarget.UpdateMessageTarget
import org.gammf.collabora.util.UpdateMessageType.UpdateMessageType

/**
  * Simple implementation of an update message
  * @param target the target of the message (note, module, collaboration, member)
  * @param messageType the type of the message (creation, update, deletion)
  * @param user the user that creates the update
  * @param note the note affected
  * @param module the module affected
  * @param collaboration the collaboration affected, used for containing the collaboration id
  * @param member the collaboration member affected
  */
case class UpdateMessageImpl(target: UpdateMessageTarget, messageType: UpdateMessageType, user: String, note: Option[Note] = None,
                             module: Option[Module] = None, collaboration: Option[Collaboration] = None,
                             member: Option[CollaborationUser] = None, collaborationId: Option[String]) extends UpdateMessage {

}