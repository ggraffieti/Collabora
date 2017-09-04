package org.gammf.collabora.authentication

import akka.actor.{ActorRef, ActorSystem}
import akka.http.scaladsl.model.HttpResponse
import akka.http.scaladsl.model.StatusCodes.{BadRequest, OK}
import akka.pattern.ask
import akka.http.scaladsl.{Http, server}
import akka.stream.ActorMaterializer
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.directives.Credentials
import akka.util.Timeout
import org.gammf.collabora.authentication.messages._
import org.gammf.collabora.database.messages.AuthenticationMessage
import org.gammf.collabora.util.User
import play.api.libs.json.{JsError, JsSuccess, Json}

import scala.concurrent.{ExecutionContextExecutor, Future}
import scala.concurrent.duration._

import scala.concurrent.ExecutionContext.Implicits.global

object AuthenticationServer {

  var authenticationActor: ActorRef = _

  implicit val timeout: Timeout = Timeout(5 seconds)

  val route: server.Route = {
    path("login") {
      authenticateBasicAsync(realm = "login", myUserPassAuthenticator) { user =>
        get {
          authenticationActor ! SendAllCollaborationsMessage(user.username)
          complete(Json.toJson(user).toString)
        }
      }
    } ~
    path("signin") {
      post {
        entity(as[String]) { jsonString =>
            Json.parse(jsonString).validate[User] match {
              case user: JsSuccess[User] => complete {
                (authenticationActor ? SigninMessage(user.value)).mapTo[SigninResponseMessage].map(message =>
                  if (message.ok) {
                    authenticationActor ! CreatePrivateCollaborationMessage(user.value.username)
                    HttpResponse(OK)
                  } else HttpResponse(BadRequest, entity = "username already present"))
              }
              case _: JsError => complete(HttpResponse(BadRequest, entity = "Data passed cannot be unmarshalled to User"))
            }
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

  private def myUserPassAuthenticator(credentials: Credentials): Future[Option[User]] =
    credentials match {
      case p @ Credentials.Provided(id) =>
        (authenticationActor ? LoginMessage(id)).mapTo[AuthenticationMessage].map(message => {
          if (message.user.isDefined && p.verify(message.user.get.hashedPassword)) Some(message.user.get)
          else None
        }
        )
      case _ => Future.successful(None)
    }
}
