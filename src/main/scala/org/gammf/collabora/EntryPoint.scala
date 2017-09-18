package org.gammf.collabora

import org.gammf.collabora.yellowpages.ActorContainer
import org.gammf.collabora.yellowpages.messages.{ActorRequestMessage, ActorResponseOKMessage, HierarchyRequestMessage}
import akka.pattern.ask
import akka.util.Timeout
import org.gammf.collabora.authentication.AuthenticationServer
import org.gammf.collabora.yellowpages.TopicElement._
import org.gammf.collabora.yellowpages.ActorService._
import org.gammf.collabora.yellowpages.util.Topic
import org.gammf.collabora.authentication.LOCALHOST_ADDRESS

import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.language.postfixOps

object EntryPoint extends App {
  implicit val timeout: Timeout = Timeout(5 seconds)

  ActorContainer.init()
  ActorContainer.createAll()

  (ActorContainer.rootYellowPages ? ActorRequestMessage(Topic() :+ Authentication, Bridging))
    .mapTo[ActorResponseOKMessage].map {
    case response: ActorResponseOKMessage => AuthenticationServer.start(ActorContainer.actorSystem, response.actor, if(args.length == 0) LOCALHOST_ADDRESS else args(0))
    case _ => System.exit(0)
  }

  Thread.sleep(1000)

  ActorContainer.rootYellowPages ! HierarchyRequestMessage(0)
}
