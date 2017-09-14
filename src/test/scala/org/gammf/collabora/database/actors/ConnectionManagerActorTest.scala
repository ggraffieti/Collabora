package org.gammf.collabora.database.actors

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit}
import com.newmotion.akka.rabbitmq.{ConnectionActor, ConnectionFactory}
import org.gammf.collabora.TestUtil
import org.gammf.collabora.database.messages.{AskConnectionMessage, GetConnectionMessage}
import org.gammf.collabora.yellowpages.ActorCreator
import org.gammf.collabora.yellowpages.ActorService.ConnectionHandler
import org.gammf.collabora.yellowpages.actors.YellowPagesActor
import org.gammf.collabora.yellowpages.messages.{RegistrationRequestMessage, RegistrationResponseMessage}
import org.gammf.collabora.yellowpages.TopicElement._
import org.gammf.collabora.yellowpages.util.Topic
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

import scala.concurrent.duration._

class ConnectionManagerActorTest extends TestKit (ActorSystem("CollaboraServer")) with WordSpecLike  with Matchers with BeforeAndAfterAll with ImplicitSender {

  val CONNECTION_ACTOR_NAME = "RabbitConnection"
  val MONGO_CONNECTION_ACTOR_NAME = "MongoConnectionManager"

  val actorCreator = new ActorCreator(system)
  val rootYellowPages = actorCreator.getYellowPagesRoot

  val factory = new ConnectionFactory()
  val rabbitConnection = system.actorOf(ConnectionActor.props(factory), CONNECTION_ACTOR_NAME)
  rootYellowPages ! RegistrationRequestMessage(rabbitConnection, CONNECTION_ACTOR_NAME, Topic() :+ Communication :+ RabbitMQ, ConnectionHandler)
  val dbConnectionActor = system.actorOf(ConnectionManagerActor.printerProps(rootYellowPages, Topic() :+ Database, MONGO_CONNECTION_ACTOR_NAME))

  override def afterAll(): Unit = {
    TestKit.shutdownActorSystem(system)
  }

  "A ConnectionManager actor" should {

    "send back connection message correctly" in {
      within(TestUtil.TASK_WAIT_TIME seconds) {
        dbConnectionActor ! new AskConnectionMessage()
        expectMsgType[RegistrationResponseMessage]
      }
    }
  }

}
