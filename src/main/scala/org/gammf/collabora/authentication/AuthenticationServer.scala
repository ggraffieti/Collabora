package org.gammf.collabora.authentication

import akka.actor.{ActorRef, ActorSystem}
import akka.pattern.ask
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.directives.Credentials
import akka.util.Timeout
import org.gammf.collabora.authentication.messages.LoginMessage
import org.gammf.collabora.database.messages.AuthenticationMessage

import scala.concurrent.{ExecutionContextExecutor, Future}
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

object AuthenticationServer {

  var dbMasterActor: ActorRef = _

  val route = {
    path("login") {
      authenticateBasicAsync(realm = "login", myUserPassAuthenticator) { _ =>
        get {
          complete("OK")
        }
      }
    }
  }


  def start(actorSystem: ActorSystem, dbActor: ActorRef) {

    dbMasterActor = dbActor

    implicit val system: ActorSystem = actorSystem

    implicit val materializer: ActorMaterializer = ActorMaterializer()
    // needed for the future flatMap/onComplete in the end
    implicit val executionContext: ExecutionContextExecutor = system.dispatcher

    Http().bindAndHandle(route, "localhost", 9894)

    println(s"Server online at http://localhost:9894/\n")
  }

  private def myUserPassAuthenticator(credentials: Credentials): Future[Option[String]] =
    credentials match {
      case p @ Credentials.Provided(id) =>
        implicit val timeout: Timeout = Timeout(5 seconds)
        (dbMasterActor ? LoginMessage(id)).mapTo[AuthenticationMessage].map(message => {
          if (message.loginInfo.isDefined && p.verify(message.loginInfo.get.hashedPassword)) Some("OK")
          else None
        }
        )
      case _ => Future.successful(None)
    }
}
