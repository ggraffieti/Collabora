package org.gammf.collabora.authentication

import akka.actor.{ActorRef, Props}
import akka.http.scaladsl.model.{HttpMethods, HttpRequest, StatusCodes}
import akka.http.scaladsl.model.headers._
import akka.http.scaladsl.server.Route
import org.scalatest.{Matchers, WordSpec}
import akka.http.scaladsl.testkit.{RouteTestTimeout, ScalatestRouteTest}
import com.newmotion.akka.rabbitmq.{ConnectionActor, ConnectionFactory}
import org.gammf.collabora.{TestMessageUtil, TestUtil}
import org.gammf.collabora.authentication.actors.AuthenticationActor
import org.gammf.collabora.communication.actors._
import org.gammf.collabora.database.actors.ConnectionManagerActor
import org.gammf.collabora.database.actors.master.DBMasterActor

import scala.concurrent.duration._

class AuthenticationServerTest extends WordSpec with Matchers with ScalatestRouteTest {

  val CONNECTION_ACTOR_NAME = "rabbitmq"
  val NAMING_ACTOR_NAME = "naming"
  val CHANNEL_CREATOR_NAME = "channelCreator"
  val PUBLISHER_ACTOR_NAME = "publisher"
  val SUBSCRIBER_ACTOR_NAME = "subscriber"
  val UPDATES_RECEIVER_ACTOR_NAME = "updates-receiver"

  val dbConnectionActor: ActorRef = system.actorOf(Props[ConnectionManagerActor])
  val factory = new ConnectionFactory()
  val connection:ActorRef = system.actorOf(ConnectionActor.props(factory), CONNECTION_ACTOR_NAME)
  val naming: ActorRef = system.actorOf(Props[RabbitMQNamingActor], NAMING_ACTOR_NAME)
  val channelCreator: ActorRef = system.actorOf(Props[ChannelCreatorActor], CHANNEL_CREATOR_NAME)
  val publisherActor: ActorRef = system.actorOf(Props[PublisherActor], PUBLISHER_ACTOR_NAME)
  val collaborationMemberActor:ActorRef = system.actorOf(Props(
    new CollaborationMembersActor(connection, naming, channelCreator, publisherActor)))
  val notificationActor: ActorRef = system.actorOf(Props(new NotificationsSenderActor(connection, naming, channelCreator, publisherActor,system)))
  val dbMasterActor:ActorRef = system.actorOf(Props.create(classOf[DBMasterActor], system, notificationActor,collaborationMemberActor))
  val subscriber:ActorRef = system.actorOf(Props[SubscriberActor], SUBSCRIBER_ACTOR_NAME)
  val updatesReceiver:ActorRef = system.actorOf(Props(
    new UpdatesReceiverActor(connection, naming, channelCreator, subscriber, dbMasterActor)), UPDATES_RECEIVER_ACTOR_NAME)
  val authenticationActor: ActorRef = system.actorOf(Props.create(classOf[AuthenticationActor], dbMasterActor))

  AuthenticationServer.start(system, authenticationActor)

  implicit val timeout: RouteTestTimeout = RouteTestTimeout(TestUtil.TASK_WAIT_TIME seconds)

  val insertUser = TestMessageUtil.insertUserRequest_AuthServerTest

  val postRequest = HttpRequest(
    method = HttpMethods.POST,
    uri = TestUtil.SIGNIN_ACTION,
    entity = insertUser
  )

  "The authentication server" should {

    "authenticate the user" in {
      Get(TestUtil.LOGIN_ACTION) ~> addCredentials(BasicHttpCredentials(TestUtil.USER_ID, TestUtil.CORRECT_PASSWORD)) ~> AuthenticationServer.route ~> check {
        status shouldEqual StatusCodes.OK
      }
    }

    "reject empty credentials" in {
      Get(TestUtil.LOGIN_ACTION) ~> Route.seal(AuthenticationServer.route) ~> check {
        status shouldEqual StatusCodes.Unauthorized
        responseAs[String] shouldEqual "The resource requires authentication, which was not supplied with the request"
        header[`WWW-Authenticate`].get.challenges.head shouldEqual HttpChallenge(TestUtil.HTTP_BASIC_CHALLENGE, Some(TestUtil.HTTP_LOGIN), Map(TestUtil.CHARSET -> TestUtil.STRING_ENCODING))
      }
    }

    "not authenticate user if password is wrong" in {
      Get(TestUtil.LOGIN_ACTION) ~> addCredentials(BasicHttpCredentials(TestUtil.USER_ID, TestUtil.WRONG_PASSWORD)) ~>
        Route.seal(AuthenticationServer.route) ~> check {
        status shouldEqual StatusCodes.Unauthorized
        responseAs[String] shouldEqual "The supplied authentication is invalid"
        header[`WWW-Authenticate`].get.challenges.head shouldEqual HttpChallenge(TestUtil.HTTP_BASIC_CHALLENGE, Some(TestUtil.HTTP_LOGIN), Map(TestUtil.CHARSET -> TestUtil.STRING_ENCODING))
      }
    }

    "not authenticate user if username not exists" in {
      Get(TestUtil.LOGIN_ACTION) ~> addCredentials(BasicHttpCredentials(TestUtil.WRONG_USERNAME, TestUtil.CORRECT_PASSWORD)) ~>
        Route.seal(AuthenticationServer.route) ~> check {
        status shouldEqual StatusCodes.Unauthorized
        responseAs[String] shouldEqual "The supplied authentication is invalid"
        header[`WWW-Authenticate`].get.challenges.head shouldEqual HttpChallenge(TestUtil.HTTP_BASIC_CHALLENGE, Some(TestUtil.HTTP_LOGIN), Map(TestUtil.CHARSET -> TestUtil.STRING_ENCODING))
      }
    }

    "sign in a new User" in {
      Post(TestUtil.SIGNIN_ACTION, insertUser) ~> Route.seal(AuthenticationServer.route) ~> check {
        status shouldEqual StatusCodes.OK
      }
    }

    "reject a signin if the username is already used" in {
      Post(TestUtil.SIGNIN_ACTION, insertUser) ~> Route.seal(AuthenticationServer.route) ~> check {
        status shouldEqual StatusCodes.BadRequest
        responseAs[String] shouldEqual "username already present"
      }
    }

    "reject a malformed request" in {
      Post(TestUtil.SIGNIN_ACTION, TestMessageUtil.emptyRequest_AuthServerTest) ~> Route.seal(AuthenticationServer.route) ~> check {
        status shouldEqual StatusCodes.BadRequest
        responseAs[String] shouldEqual "Data passed cannot be unmarshalled to User"
      }
    }
  }
}
