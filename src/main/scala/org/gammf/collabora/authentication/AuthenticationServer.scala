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

/**
  * A HTTP Server, that manage login and registration of users in the system.
  * The server is listen in 2 path: login, to perform login, and signin to perform registration.
  * For the login the user have to sent a GET request, containing in the HTTP header Authentication the
  * username and the hashed password.
  * For the registration the user have to sent a POST request, containint in its body all the data about the user,
  * and the hashed password.
  */
object AuthenticationServer {

  implicit val timeout: Timeout = Timeout(5 seconds)

  private[this] var authenticationActor: ActorRef = _

  /**
    * Represents the route of this server. The server listen to /login and /signin.
    * In /login only GET requests are allowed.
    * In /signin only POST requests are allowed.
    */
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


  /**
    * Start the server.
    * @param actorSystem the actor system of the application.
    * @param authActor the authentication actor, used as interface with the actor system.
    */
  def start(actorSystem: ActorSystem, authActor: ActorRef) {

    authenticationActor = authActor

    implicit val system: ActorSystem = actorSystem
    implicit val materializer: ActorMaterializer = ActorMaterializer()
    implicit val executionContext: ExecutionContextExecutor = system.dispatcher

    Http().bindAndHandle(route, SERVER_IP_ADDRESS, SERVER_PORT)

    println(s"Server online at http://$SERVER_IP_ADDRESS:$SERVER_PORT\n")
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
