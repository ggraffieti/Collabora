package org.gammf.collabora.authentication

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{ContentTypes, HttpEntity}
import akka.stream.ActorMaterializer
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.directives.Credentials

import scala.concurrent.{ExecutionContextExecutor, Future}

import scala.concurrent.ExecutionContext.Implicits.global

object AuthenticationServer {

  def myUserPassAuthenticator(credentials: Credentials): Future[Option[String]] =
    credentials match {
      case p @ Credentials.Provided(id) =>
        Future {
          
          // potentially
          if (p.verify("p4ssw0rd")) Some(id)
          else None
        }
      case _ => Future.successful(None)
    }

  def start(implicit actorSystem: ActorSystem) {

    implicit val materializer: ActorMaterializer = ActorMaterializer()
    // needed for the future flatMap/onComplete in the end
    implicit val executionContext: ExecutionContextExecutor = actorSystem.dispatcher

    val route = {
      path("login") {
        get {
          complete("WELCOME")
        }
      } ~
      path("signin") {
        post {
          complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, "<h1>Say hello to akka-http</h1>"))
        }
      }
    }

    Http().bindAndHandle(route, "localhost", 8080)

    println(s"Server online at http://localhost:8080/\n")
  }
}
