package org.gammf.collabora

/**
  * Created by Mattia on 04/09/2017.
  *
  * @version 1.0
  *
  * This object represent a message util for tests.
  * All messages used in tests are contained in this object.
  *
  */
object TestMessageUtil {

  val messageNotificationsSenderActorTest =
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

  val startMessageNotificationsSenderActorTest =
    """{"target":"NOTE","messageType":"CREATION","user":"JDoe","note":{"id":"""

  val endMessageNotificationsSenderActorTest =
    """"content":"refactoring in communication actor tests","expiration":"2017-08-07T08:01:17.171+02:00","location":{"latitude":546,"longitude":324},"previousNotes":["5980710df27da3fcfe0ac88e","5980710df27da3fcfe0ac88f"],"state":{"definition":"done","responsible":"JDoe"}},"collaborationId":"59806a4af27da3fcfe0ac0ca"}""".stripMargin.replaceAll("\n", " ")

  val messageSubscriberActorTest =
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

  val collaborationMembersActorTestMessage =
    """
      |{
      | "messageType": "CREATION",
      | "target" : "MEMBER",
      | "user" : "maffone",
      | "member": {
      |             "user": "maffone",
      |             "right": "WRITE"
      |           },
      | "collaborationId":"59804868f27da3fcfe0a8e20"
      | }
    """.stripMargin.replaceAll("\n", " ")

  val startMsgNotifCollaborationMembersActorTest =
    """{"target":"MEMBER","messageType":"CREATION","user":"maffone","member""""

  val startMsgCollabCollaborationMembersActorTest =
    """{"user":"maffone","collaboration":{"id":"59804868f27da3fcfe0a8e20","name":"Prova Project","collaborationType":"GROUP""""

  val tempMessageCollaborationMembersActorTest =
    """
      |{
      | "target":"MEMBER",
      | "messageType":"CREATION",
      | "user":"maffone",
      | "member"
    """.stripMargin.replaceAll("\n", " ")

  val insertUserRequest_AuthServerTest =
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

  val emptyRequest_AuthServerTest = "{}"

  val messageDBMasterActorTest =
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

  val messageToBeContainedDBMasterActorTest =
    """"target":"NOTE","messageType":"CREATION"""
}
