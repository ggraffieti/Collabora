package org.gammf.collabora

import org.gammf.collabora.communication.messages.{PublishCollaborationInCollaborationExchange, PublishErrorMessageInCollaborationExchange, PublishNotificationMessage}
import org.gammf.collabora.util.{Collaboration, CollaborationMessage, CollaborationRight, CollaborationType, CollaborationUser, Module, Note, NoteState, ServerErrorCode, ServerErrorMessage, UpdateMessage, UpdateMessageTarget, UpdateMessageType}

/****
  * This object represent a message util for tests.
  * All messages used in tests are contained in this object.
  *
  */
object TestMessageUtil {

  /**
    * Authentication related constants
    */
  val insertUserRequest_AuthServerTest: String =
    """
      |{
      | "username":"JDoe",
      | "email":"john.doe@email.com",
      | "name":"John",
      | "surname":"Doe",
      | "birthday":"1980-01-01T05:27:19.199+02:00",
      | "hashedPassword":"notSoHashedPassord"
      |}
    """.stripMargin.replaceAll("\n", " ")

  val emptyRequest_AuthServerTest: String = "{}"

  /**
    * Collaborations related constants
    */
  val fakeCollaborationId = "59bd381e3b00003d006b65be"

  val collaborationMessage = CollaborationMessage("fone", Collaboration(Some(fakeCollaborationId),
                                                          "collaboration",
                                                          CollaborationType.GROUP))

  val publishCollaborationMessage = PublishCollaborationInCollaborationExchange("fone", collaborationMessage)

  val serverErrorMessage = ServerErrorMessage("fone", ServerErrorCode.SERVER_ERROR)

  val publishErrorCollaborationMessage = PublishErrorMessageInCollaborationExchange("fone", serverErrorMessage)

  /**
    * Notifications related constants
     */
  val notificationNote = Note(id = Some("8sfdg9sdf8g"),
                              content = "note",
                              state = NoteState("doing", Some("fone")))
  val noteUpdateMessage= UpdateMessage(target = UpdateMessageTarget.NOTE,
                                       messageType = UpdateMessageType.CREATION,
                                       user = "fone",
                                       note = Some(notificationNote),
                                       collaborationId = Some(fakeCollaborationId))
  val publishNoteNotificationMessage = PublishNotificationMessage(fakeCollaborationId, noteUpdateMessage)


  val notificationModule = Module(Some("dfgs8d8fgs8d"), "this is a module", "doing")
  val moduleUpdateMessage = UpdateMessage(target = UpdateMessageTarget.MODULE,
                                          messageType = UpdateMessageType.UPDATING,
                                          user = "fone",
                                          module = Some(notificationModule),
                                          collaborationId = Some(fakeCollaborationId))
  val publishModuleNotificationMessage = PublishNotificationMessage(fakeCollaborationId, moduleUpdateMessage)


  val notificationCollaboration = Collaboration(id = Some("i8afsd7f6"),
                                                name = "collaboration",
                                                collaborationType = CollaborationType.GROUP)
  val collaborationUpdateMessage = UpdateMessage(target = UpdateMessageTarget.COLLABORATION,
                                                 messageType = UpdateMessageType.CREATION,
                                                 user = "fone",
                                                 collaboration = Some(notificationCollaboration),
                                                 collaborationId = Some(fakeCollaborationId))
  val publishCollaborationNotificationMessage = PublishNotificationMessage(fakeCollaborationId, collaborationUpdateMessage)


  val notificationMember = CollaborationUser("fone", CollaborationRight.ADMIN)
  val memberUpdateMessage = UpdateMessage(target = UpdateMessageTarget.MEMBER,
                                          messageType = UpdateMessageType.DELETION,
                                          user = "fone",
                                          member = Some(notificationMember),
                                          collaborationId = Some(fakeCollaborationId))
  val publishMemberNotificationMessage = PublishNotificationMessage(fakeCollaborationId, memberUpdateMessage)

  /**
    * ModuleStateChangeActor related constants
    */
  val moduleStateChangeCollaborationID = "5980482cf27da3fcfe0a8ddf"
  val moduleStateChangeModuleID = "59804ad3f27da3fcfe0a8f37"

  /**
    * GetCollaborationActor realated constant
     */
  val collaborationGetterCollaborationID = "5980482cf27da3fcfe0a8ddf"
}
