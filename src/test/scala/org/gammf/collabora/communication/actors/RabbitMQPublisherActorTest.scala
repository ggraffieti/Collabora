package org.gammf.collabora.communication.actors

import akka.actor.{ ActorSystem}
import akka.testkit.{DefaultTimeout, ImplicitSender, TestKit}
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

class RabbitMQPublisherActorTest extends TestKit (ActorSystem("CollaboraServer")) with WordSpecLike with DefaultTimeout with Matchers with BeforeAndAfterAll with ImplicitSender {


  override def beforeAll(): Unit ={
  }

  override def afterAll(): Unit = {
    TestKit.shutdownActorSystem(system)
  }


  "A Publish actor" should {

    "receive all messages only when channel is created correctly" in {

    }

  }






}
