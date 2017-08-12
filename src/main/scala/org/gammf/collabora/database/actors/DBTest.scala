package org.gammf.collabora.database.actors

import akka.actor.{ActorSystem, Props}
import org.gammf.collabora.database.messages.{DeleteNoteMessage, InsertNoteMessage, UpdateNoteMessage}
import org.gammf.collabora.util.{Location, NoteState, SimpleNote}
import org.joda.time.DateTime

object DBTest extends App {

  val system = ActorSystem("CollaboraServer")

  val dbConnectionActor = system.actorOf(Props[ConnectionManagerActor])
  val dbActor = system.actorOf(Props.create(classOf[DBWorkerNotesActor], dbConnectionActor))

  val note = SimpleNote(
    id = Some("598f68012a00002a00ff7d48"),
    content = "TROLOLOLO",
    expiration = Some(DateTime.now()),
    location = Some(Location(25.2515, 1.2547)),
    state = NoteState("done", Some("hebry")),
    previousNotes = Some(List("59806fbbf27da3fcfe0ac7bf"))
  )

  val collID = "59806a4af27da3fcfe0ac0ca"
  val username = "ggraffieti"


  //dbActor ! InsertNoteMessage(note = note, collaborationID = collID, userID = username)
  dbActor ! DeleteNoteMessage(note = note, collaborationID = collID, userID = username)


}
