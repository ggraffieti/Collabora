package org.gammf.collabora.authentication

import akka.http.scaladsl.model.{HttpMethods, HttpRequest, StatusCodes}
import akka.http.scaladsl.model.headers._
import akka.http.scaladsl.server.Route
import org.scalatest.{Matchers, WordSpec}
import akka.http.scaladsl.testkit.{RouteTestTimeout, ScalatestRouteTest}
import akka.util.Timeout
import org.gammf.collabora.authentication.actors.AuthenticationActor
import org.gammf.collabora.yellowpages.ActorContainer
import org.gammf.collabora.{TestMessageUtil, TestUtil}

import scala.concurrent.duration._
import scala.language.postfixOps
import org.gammf.collabora.yellowpages.util.Topic
import org.gammf.collabora.yellowpages.TopicElement._

class AuthenticationServerTest extends WordSpec with Matchers with ScalatestRouteTest {

  implicit protected[this] val askTimeout: Timeout = Timeout(5 second)
  implicit val timeout: RouteTestTimeout = RouteTestTimeout(TestUtil.TASK_WAIT_TIME seconds)

  val insertUser: String = TestMessageUtil.insertUserRequest_AuthServerTest

  val postRequest = HttpRequest(
    method = HttpMethods.POST,
    uri = TestUtil.SIGNIN_ACTION,
    entity = insertUser
  )

  override def beforeAll(): Unit = {
    ActorContainer.init()
    ActorContainer.createAll()
    val authenticationActor = ActorContainer.actorSystem.actorOf(AuthenticationActor.authenticationProps(ActorContainer.rootYellowPages, Topic() :+ Authentication, "Authentication"))
    AuthenticationServer.start(ActorContainer.actorSystem, authenticationActor, LOCALHOST_ADDRESS)
    Thread.sleep(200)
  }

  override def afterAll(): Unit = {
    ActorContainer.shutdown()
  }

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
