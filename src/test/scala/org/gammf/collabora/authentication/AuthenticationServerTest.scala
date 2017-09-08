package org.gammf.collabora.authentication

import akka.actor.{ActorRef, Props}
import akka.http.scaladsl.model.{HttpMethods, HttpRequest, StatusCodes}
import akka.http.scaladsl.model.headers._
import akka.http.scaladsl.server.Route
import org.scalatest.{Matchers, WordSpec}
import akka.http.scaladsl.testkit.{RouteTestTimeout, ScalatestRouteTest}
import com.newmotion.akka.rabbitmq.{ConnectionActor, ConnectionFactory}
import org.gammf.collabora.communication.actors._
import org.gammf.collabora.database.actors.ConnectionManagerActor
import org.gammf.collabora.database.actors.master.DBMasterActor

import scala.concurrent.duration._

class AuthenticationServerTest extends WordSpec with Matchers with ScalatestRouteTest {

  val dbConnectionActor: ActorRef = system.actorOf(Props[ConnectionManagerActor])
  val factory = new ConnectionFactory()
  val connection:ActorRef = system.actorOf(ConnectionActor.props(factory), "rabbitmq")
  val naming: ActorRef = system.actorOf(Props[RabbitMQNamingActor], "naming")
  val channelCreator: ActorRef = system.actorOf(Props[ChannelCreatorActor], "channelCreator")
  val publisherActor: ActorRef = system.actorOf(Props[PublisherActor], "publisher")
  val collaborationMemberActor:ActorRef = system.actorOf(Props(
    new CollaborationMembersActor(connection, naming, channelCreator, publisherActor)))
  val notificationActor: ActorRef = system.actorOf(Props(new NotificationsSenderActor(connection, naming, channelCreator, publisherActor,system)))
  val dbMasterActor:ActorRef = system.actorOf(Props.create(classOf[DBMasterActor], system, notificationActor,collaborationMemberActor))
  val subscriber:ActorRef = system.actorOf(Props[SubscriberActor], "subscriber")
  val updatesReceiver:ActorRef = system.actorOf(Props(
    new UpdatesReceiverActor(connection, naming, channelCreator, subscriber, dbMasterActor)), "updates-receiver")

  AuthenticationServer.start(system, dbMasterActor)

  implicit val timeout: RouteTestTimeout = RouteTestTimeout(5 seconds)

  val rightUser = "{\"username\":\"maffone\",\"email\":\"alfredo.maffi@studio.unibo.it\",\"name\":\"Alfredo\",\"surname\":\"Maffi\",\"birthday\":\"1994-08-09T05:27:19.199+02:00\"}"
  val insertUser = "{\"username\":\"JDoe\",\"email\":\"john.doe@email.com\",\"name\":\"John\",\"surname\":\"Doe\",\"birthday\":\"1980-01-01T05:27:19.199+02:00\",\"hashedPassword\":\"notSoHashedPassord\"}"

  val postRequest = HttpRequest(
    method = HttpMethods.POST,
    uri = "/signin",
    entity = insertUser
  )

  "The authentication server" should {

    "authenticate the user" in {
      Get("/login") ~> addCredentials(BasicHttpCredentials("maffone", "admin")) ~> AuthenticationServer.route ~> check {
        responseAs[String] shouldEqual rightUser
      }
    }

    "reject empty credentials" in {
      Get("/login") ~> Route.seal(AuthenticationServer.route) ~> check {
        status shouldEqual StatusCodes.Unauthorized
        responseAs[String] shouldEqual "The resource requires authentication, which was not supplied with the request"
        header[`WWW-Authenticate`].get.challenges.head shouldEqual HttpChallenge("Basic", Some("login"), Map("charset" -> "UTF-8"))
      }
    }

    "not authenticate user if password is wrong" in {
      Get("/login") ~> addCredentials(BasicHttpCredentials("maffone", "not_maffone_password")) ~>
        Route.seal(AuthenticationServer.route) ~> check {
        status shouldEqual StatusCodes.Unauthorized
        responseAs[String] shouldEqual "The supplied authentication is invalid"
        header[`WWW-Authenticate`].get.challenges.head shouldEqual HttpChallenge("Basic", Some("login"), Map("charset" -> "UTF-8"))
      }
    }

    "not authenticate user if username not exists" in {
      Get("/login") ~> addCredentials(BasicHttpCredentials("wrong_username", "password")) ~>
        Route.seal(AuthenticationServer.route) ~> check {
        status shouldEqual StatusCodes.Unauthorized
        responseAs[String] shouldEqual "The supplied authentication is invalid"
        header[`WWW-Authenticate`].get.challenges.head shouldEqual HttpChallenge("Basic", Some("login"), Map("charset" -> "UTF-8"))
      }
    }

    "sign in a new User" in {
      Post("/signin", insertUser) ~> Route.seal(AuthenticationServer.route) ~> check {
        status shouldEqual StatusCodes.OK
        responseAs[String] shouldEqual ""
      }
    }

    "reject a signin if the username is already used" in {
      Post("/signin", insertUser) ~> Route.seal(AuthenticationServer.route) ~> check {
        status shouldEqual StatusCodes.BadRequest
        responseAs[String] shouldEqual "username already present"
      }
    }

    "reject a malformed request" in {
      Post("/signin", "{}") ~> Route.seal(AuthenticationServer.route) ~> check {
        status shouldEqual StatusCodes.BadRequest
        responseAs[String] shouldEqual "Data passed cannot be unmarshalled to User"
      }
    }
  }
}
