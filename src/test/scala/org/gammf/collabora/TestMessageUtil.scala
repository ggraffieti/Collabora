package org.gammf.collabora

import org.gammf.collabora.communication.messages.{PublishCollaborationInCollaborationExchange, PublishErrorMessageInCollaborationExchange, PublishNotificationMessage}
import org.gammf.collabora.util.{Collaboration, CollaborationMessage, CollaborationRight, CollaborationType, CollaborationUser, Module, Note, NoteState, ServerErrorCode, ServerErrorMessage, UpdateMessage, UpdateMessageTarget, UpdateMessageType}

/****
  * This object represent a message util for tests.
  * All messages used in tests are contained in this object.
  *
  */
object TestMessageUtil {

  val messageNotificationsSenderActorTest: String =
    """
      |{
      |   "messageType": "CREATION",
      |   "collaborationId":"59806a4af27da3fcfe0ac0ca",
      |   "target" : "NOTE",
      |   "user" : "JDoe",
      |   "note": {
      |             "content" : "refactoring in communication actor tests",
      |             "expiration" : "2017-08-07T08:01:17.171+02:00",
      |             "location" : {
      |                             "latitude" : 546,
      |                             "longitude" : 324
      |                          },
      |             "previousNotes" : [ "5980710df27da3fcfe0ac88e", "5980710df27da3fcfe0ac88f" ],
      |             "state" : {
      |                         "definition" : "done",
      |                         "responsible" : "JDoe"
      |                       }
      |            }
      | }
    """.stripMargin.replaceAll("\n", " ")

  val startMessageNotificationsSenderActorTest: String =
    """{"target":"NOTE","messageType":"CREATION","user":"JDoe","note":{"id":"""

  val endMessageNotificationsSenderActorTest: String =
    """"content":"refactoring in communication actor tests","expiration":"2017-08-07T08:01:17.171+02:00","location":{"latitude":546,"longitude":324},"previousNotes":["5980710df27da3fcfe0ac88e","5980710df27da3fcfe0ac88f"],"state":{"definition":"done","responsible":"JDoe"}},"collaborationId":"59806a4af27da3fcfe0ac0ca"}""".stripMargin.replaceAll("\n", " ")

  val messageSubscriberActorTest: String =
    """
      |{
      |"messageType": "insertion",
      |"target" : "note",
      |"user" : "JDoe",
      |"note": {
      |         "content" : "setup working enviroment",
      |         "expiration" : "2017-08-07T08:01:17.171+02:00",
      |         "location" : {
      |                       "latitude" : 546,
      |                       "longitude" : 324
      |                      },
      |         "previousNotes" : [ "5980710df27da3fcfe0ac88e", "5980710df27da3fcfe0ac88f" ],
      |         "state" : {
      |                     "definition" : "done",
      |                     "username" : "JDoe"
      |                   }
      |         }
      |}
    """.stripMargin.replaceAll("\n", " ")

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

  val messageDBMasterActorTest: String =
    """
      |{
      |   "target":"NOTE",
      |   "messageType":"CREATION",
      |   "collaborationId":"59806a4af27da3fcfe0ac0ca",
      |   "user" : "JDoe",
      |   "note": {
      |             "content" : "creation of some messages for tests",
      |             "expiration" : "2017-08-07T08:01:17.171+02:00",
      |             "location" : { "latitude" : 546, "longitude" : 324 },
      |             "previousNotes" : [ "5980710df27da3fcfe0ac88e", "5980710df27da3fcfe0ac88f" ],
      |             "state" : {
      |                         "definition" : "done",
      |                         "username" : "JDoe"
      |                        }
      |            }
      | }
    """.stripMargin.replaceAll("\n", " ")

  val messageToBeContainedDBMasterActorTest: String =
    """"target":"NOTE","messageType":"CREATION"""

  val jsonCollaborationMessage: String =
    """
      |{
      |  "user" : "fone",
      |  "collaboration" : {
      |    "id": "59bd381e3b00003d006b65be",
      |    "name": "group",
      |    "collaborationType": "GROUP",
      |    "users": [ {
      |    "user": "fone",
      |    "right": "ADMIN"
      |    }]
      |  }
      |}
    """.stripMargin.replaceAll("\n", " ")

  val collaborationId = "59bd381e3b00003d006b65be"

  val collaborationMessage = CollaborationMessage("fone", Collaboration(Some(collaborationId),
                                                          "collaboration",
                                                          CollaborationType.GROUP))

  val publishCollaborationMessage = PublishCollaborationInCollaborationExchange("fone", collaborationMessage)

  val serverErrorMessage = ServerErrorMessage("fone", ServerErrorCode.SERVER_ERROR)

  val publishErrorCollaborationMessage = PublishErrorMessageInCollaborationExchange("fone", serverErrorMessage)

  //-----------------------------------
  val notificationNote = Note(id = Some("8sfdg9sdf8g"),
                              content = "note",
                              state = NoteState("doing", Some("fone")))
  val noteUpdateMessage= UpdateMessage(target = UpdateMessageTarget.NOTE,
                                       messageType = UpdateMessageType.CREATION,
                                       user = "fone",
                                       note = Some(notificationNote),
                                       collaborationId = Some(collaborationId))
  val publishNoteNotificationMessage = PublishNotificationMessage(collaborationId, noteUpdateMessage)


  val notificationModule = Module(Some("dfgs8d8fgs8d"), "this is a module", "doing")
  val moduleUpdateMessage = UpdateMessage(target = UpdateMessageTarget.MODULE,
                                          messageType = UpdateMessageType.UPDATING,
                                          user = "fone",
                                          module = Some(notificationModule),
                                          collaborationId = Some(collaborationId))
  val publishModuleNotificationMessage = PublishNotificationMessage(collaborationId, moduleUpdateMessage)


  val notificationCollaboration = Collaboration(id = Some("i8afsd7f6"),
                                                name = "collaboration",
                                                collaborationType = CollaborationType.GROUP)
  val collaborationUpdateMessage = UpdateMessage(target = UpdateMessageTarget.COLLABORATION,
                                                 messageType = UpdateMessageType.CREATION,
                                                 user = "fone",
                                                 collaboration = Some(notificationCollaboration),
                                                 collaborationId = Some(collaborationId))
  val publishCollaborationNotificationMessage = PublishNotificationMessage(collaborationId, collaborationUpdateMessage)


  val notificationMember = CollaborationUser("fone", CollaborationRight.ADMIN)
  val memberUpdateMessage = UpdateMessage(target = UpdateMessageTarget.MEMBER,
                                          messageType = UpdateMessageType.DELETION,
                                          user = "fone",
                                          member = Some(notificationMember),
                                          collaborationId = Some(collaborationId))
  val publishMemberNotificationMessage = PublishNotificationMessage(collaborationId, memberUpdateMessage)
}
