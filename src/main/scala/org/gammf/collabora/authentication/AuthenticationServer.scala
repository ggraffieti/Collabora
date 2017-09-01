package org.gammf.collabora.authentication

import akka.actor.{ActorRef, ActorSystem}
import akka.pattern.ask
import akka.http.scaladsl.{Http, server}
import akka.stream.ActorMaterializer
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.directives.Credentials
import akka.util.Timeout
import org.gammf.collabora.authentication.messages.{LoginMessage, SendAllCollaborationsMessage}
import org.gammf.collabora.database.messages.AuthenticationMessage

import scala.concurrent.{ExecutionContextExecutor, Future}
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

object AuthenticationServer {

  var authenticationActor: ActorRef = _

  val route: server.Route = {
    path("login") {
      authenticateBasicAsync(realm = "login", myUserPassAuthenticator) { username =>
        get {
          authenticationActor ! SendAllCollaborationsMessage(username)
          complete("OK")
        }
      }
    }
  }


  def start(actorSystem: ActorSystem, authActor: ActorRef) {

    authenticationActor = authActor

    implicit val system: ActorSystem = actorSystem
    implicit val materializer: ActorMaterializer = ActorMaterializer()
    implicit val executionContext: ExecutionContextExecutor = system.dispatcher

    Http().bindAndHandle(route, "localhost", 9894)

    println(s"Server online at http://localhost:9894/\n")
  }

  private def myUserPassAuthenticator(credentials: Credentials): Future[Option[String]] =
    credentials match {
      case p @ Credentials.Provided(id) =>
        implicit val timeout: Timeout = Timeout(5 seconds)
        (authenticationActor ? LoginMessage(id)).mapTo[AuthenticationMessage].map(message => {
          if (message.loginInfo.isDefined && p.verify(message.loginInfo.get.hashedPassword)) Some(id)
          else None
        }
        )
      case _ => Future.successful(None)
    }
}
