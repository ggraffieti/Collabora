package org.gammf.collabora.authentication.actors

import akka.actor.{Actor, ActorRef}
import akka.pattern.{ask, pipe}
import akka.util.Timeout
import org.gammf.collabora.authentication.messages.LoginMessage
import org.gammf.collabora.database.messages.AuthenticationMessage

import scala.concurrent.duration._

import scala.concurrent.ExecutionContext.Implicits.global

class AuthenticationActor(private val dbActor: ActorRef) extends Actor {

  private implicit val timeout: Timeout = Timeout(5 seconds)

  override def receive: Receive = {

    case message: LoginMessage => (dbActor ? message).mapTo[AuthenticationMessage] pipeTo sender

  }
}
