package org.gammf.collabora.communication

import akka.actor.{Actor, ActorSystem, Props}
import org.gammf.collabora.database.actors.{ConnectionManagerActor, DBActor, PrintActor}
import org.gammf.collabora.database.messages.InsertNoteMessage
import org.gammf.collabora.util.SimpleNote
import play.api.libs.json.{JsError, JsSuccess, Json}

/**
  * Created by mperuzzi on 05/08/17.
  */
class JsonConverterActor extends Actor {

  override def receive: Receive = {
    case ClientUpdateMessage(text) =>
      val json = Json.parse(text)
      json.validate[SimpleNote] match {
        case note: JsSuccess[SimpleNote] => println(note.value)
        case error: JsError => println("JsonError: " + error)
      }
    case _ => println("[JsonConverterActor] Huh?")
  }

}

object TestJsonConverterActor extends App {
  val json = """
  { "id" : "5980710df27da3fcfe0ac88d",
    "content" : "setup working enviroment",
    "expiration" : "2017-08-07T06:01:17.171Z",
    "state" : { "definition" : "done", "username" : "maffone" }
  }
  """
  implicit val system = ActorSystem()
  val converter = system.actorOf(Props[JsonConverterActor], "converter")
  //converter ! ClientUpdateMessage(json)

  val connectionActor = system.actorOf(Props[ConnectionManagerActor])
  val printActor = system.actorOf(Props[PrintActor])
  val dbActor = system.actorOf(Props.create(classOf[DBActor], connectionActor, printActor))

  val convertedJson = Json.parse(json)
  convertedJson.validate[SimpleNote] match {
    case note: JsSuccess[SimpleNote] =>
      println(note)
      dbActor ! new InsertNoteMessage(note.value)
    case error: JsError => println("JsonError: " + error)
  }
}
