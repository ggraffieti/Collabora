package org.gammf.collabora

import java.util.Calendar

import akka.actor.{ActorSystem, Props}
import org.gammf.collabora.database.actors.{ConnectionManagerActor, DBActor, PrintActor}
import org.gammf.collabora.database.messages.{InsertNoteMessage, RequestAllNotesMessage}
import org.gammf.collabora.util.SimpleNote

object Test extends App {
  val system = ActorSystem("HelloSystem")
  val connectionActor = system.actorOf(Props[ConnectionManagerActor])
  val printActor = system.actorOf(Props[PrintActor])
  val dbActor = system.actorOf(Props.create(classOf[DBActor], connectionActor, printActor))

  //val note: SimpleNote = new SimpleNote(content = "Insertion test", state = "toDo", location = Some(13.2541, 45.2541), previousNotes = Some(List("5980710df27da3fcfe0ac88d", "59806ff7f27da3fcfe0ac7d2")), expiration = Some(Calendar.getInstance.getTime))

  //dbActor ! new InsertNoteMessage(note)
  //dbActor ! new RequestAllNotesMessage()
}
