package org.gammf.collabora.database.actors

import akka.actor.{ActorSystem, Props}
import org.gammf.collabora.database.actors.DBTest.module
import org.gammf.collabora.database.messages._
import org.gammf.collabora.util.{Location, NoteState, SimpleModule, SimpleNote}
import org.joda.time.DateTime

object DBTest extends App {

  val system = ActorSystem("CollaboraServer")

  val dbConnectionActor = system.actorOf(Props[ConnectionManagerActor])
  val dbActor = system.actorOf(Props.create(classOf[DBWorkerModulesActor], dbConnectionActor))

  val module = SimpleModule(
    id = Some("598f77572a00002a000435c7"),
    state = "Done",
    content = "CACCA DI MERDAAAAAAAAAAAAA",
    previousModules = Some(List("598f77572a00002a000435c7", "598f77572a00002a000435c7"))
  )

  val collID = "59806a4af27da3fcfe0ac0ca"
  val username = "ggraffieti"


  //dbActor ! UpdateModuleMessage(module = module, collaborationID = collID, userID = username)
  dbActor ! DeleteModuleMessage(module = module, collaborationID = collID, userID = username)


}
