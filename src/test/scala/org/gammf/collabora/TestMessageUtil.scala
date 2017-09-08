package org.gammf.collabora

/**
  * Created by Mattia on 04/09/2017.
  */
object TestMessageUtil {

  val messageNotificationsSenderActorTest =
    """
      |{
      |   "messageType": "CREATION",
      |   "collaborationId":"59806a4af27da3fcfe0ac0ca",
      |   "target" : "NOTE",
      |   "user" : "maffone",
      |   "note": {
      |             "content" :
      |             "prova test",
      |             "expiration" : "2017-08-07T08:01:17.171+02:00",
      |             "location" : {
      |                             "latitude" : 546,
      |                             "longitude" : 324
      |                          },
      |             "previousNotes" : [ "5980710df27da3fcfe0ac88e", "5980710df27da3fcfe0ac88f" ],
      |             "state" : {
      |                         "definition" : "done",
      |                         "responsible" : "maffone"
      |                       }
      |            }
      | }
    """.stripMargin

  val startMessageNotificationsSenderActorTest =
    """
      |{
      |   "target":"NOTE",
      |   "messageType":"CREATION",
      |   "user":"maffone",
      |   "note":{
      |           "id":
    """.stripMargin

  val endMessageNotificationsSenderActorTest =
    """
      | "content":"prova test",
      | "expiration":"2017-08-07T08:01:17.171+02:00",
      | "location":{
      |             "latitude":546,
      |             "longitude":324
      |            },
      | "previousNotes":["5980710df27da3fcfe0ac88e","5980710df27da3fcfe0ac88f"],
      | "state":{
      |           "definition":"done",
      |           "responsible":"maffone"
      |         }
      |},
      |"collaborationId":"59806a4af27da3fcfe0ac0ca"}
    """.stripMargin
  val messageSubscriberActorTest =
    """
      |{
      |"messageType\": "insertion",
      |"target" : "note",
      |"user" : "maffone",
      |"note": {
      |         "content\" : "setup working enviroment",
      |         "expiration" : "2017-08-07T08:01:17.171+02:00",
      |         "location" : {
      |                       "latitude" : 546,
      |                       "longitude" : 324
      |                      },
      |         "previousNotes" : [ "5980710df27da3fcfe0ac88e", "5980710df27da3fcfe0ac88f" ],
      |         "state" : {
      |                     "definition" : "done",
      |                     "username" : "maffone"
      |                   }
      |         }
      |}
    """.stripMargin


  val messageDBMasterActorTest =
    """
      |{
      |   "target":"NOTE",
      |   "messageType":"CREATION",
      |   "collaborationId":"59806a4af27da3fcfe0ac0ca",
      |   "user" : "maffone",
      |   "note": {
      |             "content" : "c'ho un nervoso che ti ciacherei la testa",
      |             "expiration" : "2017-08-07T08:01:17.171+02:00",
      |             "location" : { "latitude" : 546, "longitude" : 324 },
      |             "previousNotes" : [ "5980710df27da3fcfe0ac88e", "5980710df27da3fcfe0ac88f" ],
      |             "state" : {
      |                         "definition" : "done",
      |                         "username" : "maffone"
      |                        }
      |            }
      | }
    """.stripMargin

  val messageToBeContainedDBMasterActorTest = "\"target\":\"NOTE\",\"messageType\":\"CREATION"
/*  val messageToBeContainedDBMasterActorTest =
    """
      | "target":"NOTE",
      | "messageType":"CREATION"
    """.stripMargin
  */
}
