package org.gammf.collabora

import akka.actor.{ActorSystem, Props}
import com.newmotion.akka.rabbitmq.{ConnectionActor, ConnectionFactory}
import org.gammf.collabora.authentication.AuthenticationServer
import org.gammf.collabora.authentication.actors.AuthenticationActor
import org.gammf.collabora.communication.actors._
import org.gammf.collabora.database.actors.ConnectionManagerActor
import org.gammf.collabora.database.actors.master._
import org.gammf.collabora.database.actors.worker._
import org.gammf.collabora.yellowpages.ActorCreator
import org.gammf.collabora.yellowpages.actors.{PrinterActor, YellowPagesActor}
import org.gammf.collabora.yellowpages.util.Topic
import org.gammf.collabora.yellowpages.TopicElement._
import org.gammf.collabora.yellowpages.ActorService._
import org.gammf.collabora.yellowpages.messages.{HierarchyRequestMessage, RegistrationRequestMessage}

object EntryPoint extends App {
  val system = ActorSystem("CollaboraServer")

  val actorCreator = new ActorCreator(system)
  actorCreator.startCreation
  val rootYellowPages = actorCreator.getYellowPagesRoot

  Thread.sleep(1000)

  rootYellowPages ! HierarchyRequestMessage(0)
}
