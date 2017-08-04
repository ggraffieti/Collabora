package org.gammf.collabora.database.actors

import akka.actor.Actor
import org.gammf.collabora.database.messages.PrintMessage

/**
  * A dumb actor, that print on console the content of messages. Used only for debug purpose, it will be deleted soon.
  */
class PrintActor extends Actor {
  override def receive: Receive = {
    case m: PrintMessage => println(m.message)
    case _ => // do nothing
  }
}
