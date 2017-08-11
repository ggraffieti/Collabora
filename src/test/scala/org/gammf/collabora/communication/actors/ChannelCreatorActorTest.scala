package org.gammf.collabora.communication.actors

import akka.actor.ActorSystem
import akka.testkit.{ImplicitSender, TestKit}
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

class ChannelCreatorActorTest extends TestKit (ActorSystem("CollaboraServer")) with WordSpecLike with Matchers with BeforeAndAfterAll with ImplicitSender{

    override def afterAll(): Unit = {
      TestKit.shutdownActorSystem(system)
    }

    "A ChannelCreator actor" must {

      "respond back" in {

      }

    }
}
