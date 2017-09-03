package org.gammf.collabora.database.actors

import akka.actor.{ActorRef, ActorSystem, Props}
  import akka.testkit.{ImplicitSender, TestKit}
  import org.gammf.collabora.database.messages.{AskConnectionMessage, GetConnectionMessage}
  import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}
  import scala.concurrent.duration._

  class ConnectionManagerActorTest extends TestKit (ActorSystem("CollaboraServer")) with WordSpecLike  with Matchers with BeforeAndAfterAll with ImplicitSender {

    val TASK_WAIT_TIME = 5

    val dbConnectionActor:ActorRef = system.actorOf(Props[ConnectionManagerActor])

    override def afterAll(): Unit = {
      TestKit.shutdownActorSystem(system)
    }

    "A ConnectionManager actor" should {

      "send back connection message correctly" in {
        within(TASK_WAIT_TIME seconds) {
          dbConnectionActor ! new AskConnectionMessage()
          expectMsgType[GetConnectionMessage]
        }
      }
    }

}
