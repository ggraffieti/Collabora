package org.gammf.collabora.yellowpages.actors

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit}
import org.scalatest._
import akka.util.Timeout
import org.gammf.collabora.communication.actors.{ChannelCreatorActor, PublisherActor, RabbitMQNamingActor}
import org.gammf.collabora.yellowpages.ActorService._
import org.gammf.collabora.yellowpages.messages.{ActorRequestMessage, _}
import org.gammf.collabora.yellowpages.util.Topic
import org.gammf.collabora.yellowpages.TopicElement._

import scala.concurrent.duration._

class YellowPagesActorTest extends TestKit(ActorSystem("CollaboraServer")) with ImplicitSender
  with WordSpecLike with Matchers with BeforeAndAfterAll {
  implicit val timeout: Timeout = Timeout(5 seconds)


  val root: ActorRef = system.actorOf(YellowPagesActor.rootProps())

  override def afterAll {
    TestKit.shutdownActorSystem(system)
  }

  "A just-initialized root yellow pages actor" must {
    "respond with an ActorResponseErrorMessage if an actor-ref is requested" in {
      root ! ActorRequestMessage(Topic() :+ Database, Publishing)
      expectMsgType[ActorResponseErrorMessage]
    }
  }

  val naming: ActorRef = system.actorOf(Props[RabbitMQNamingActor], "naming")

  "The root yellow pages actor" must {
    "respond with a RegistrationResponseMessage if an actor with (topic = General, service = Naming) asks to be registered" in {
      root ! RegistrationRequestMessage(naming, "Naming Actor", Topic() :+ General, Naming)
      expectMsgType[RegistrationResponseMessage]
    }
  }

  "Now, the root yellow pages actor" must {
    "respond with an ActorResponseOKMessage if the following actor request (topic = General, service = Naming) is sent to it" in {
      root ! ActorRequestMessage(Topic() :+ General, Naming)
      expectMsg(ActorResponseOKMessage(naming, Topic() :+ General, Naming))
    }
    "respond with an ActorResponseErrorMessage if it receives an ActorRequestMessage requesting a different service from \"Naming\"" in {
      root ! ActorRequestMessage(Topic() :+ General, Printing)
      expectMsgType[ActorResponseErrorMessage]
      root ! ActorRequestMessage(Topic() :+ General, ChannelCreating)
      expectMsgType[ActorResponseErrorMessage]
      root ! ActorRequestMessage(Topic() :+ General, CollaborationSending)
      expectMsgType[ActorResponseErrorMessage]
      root ! ActorRequestMessage(Topic() :+ General, NotificationSending)
      expectMsgType[ActorResponseErrorMessage]
      root ! ActorRequestMessage(Topic() :+ General, Worker)
      expectMsgType[ActorResponseErrorMessage]
      root ! ActorRequestMessage(Topic() :+ General, Master)
      expectMsgType[ActorResponseErrorMessage]
    }
    "respond with another ActorResponseErrorMessage if it receives and ActorRequestMessage requesting a \"Naming\" service in a topic that is > than \"General\" (more structured)" in {
      root ! ActorRequestMessage(Topic() :+ General :+ RabbitMQ, Naming)
      expectMsgType[ActorResponseErrorMessage]
      root ! ActorRequestMessage(Topic() :+ RabbitMQ :+ General, Naming)
      expectMsgType[ActorResponseErrorMessage]
      root ! ActorRequestMessage(Topic() :+ Communication, Naming)
      expectMsgType[ActorResponseErrorMessage]
    }
  }

  val channelCreator: ActorRef = system.actorOf(Props[ChannelCreatorActor], "channelCreator")
  val publisherActor: ActorRef = system.actorOf(Props[PublisherActor], "publisher")

  "At this point, the root yellow pages" should {
    "have no problems with registering the two new actors in -> (topic = Communication, service = ChannelCreating) and (topic = Communication.RabbitMQ, service = ChannelCreating)" in {
      root ! RegistrationRequestMessage(publisherActor, "publisher", Topic() :+ Communication, Publishing)
      expectMsgType[RegistrationResponseMessage]
      root ! RegistrationRequestMessage(channelCreator, "channelCreator", Topic() :+ Communication :+ RabbitMQ, ChannelCreating)
      expectMsgType[RegistrationResponseMessage]
    }
  }

  "Now, the root yellow pages" should {
    "respond with ActorResponseOKMessage if an ActorRequestMessage having (topic = Communication.RabbitMQ, service = ChannelCreating) is sent to it" in {
      root ! ActorRequestMessage(Topic() :+ Communication, ChannelCreating)
      expectMsgType[ActorResponseErrorMessage]
      root ! ActorRequestMessage(Topic() :+ Communication :+ RabbitMQ, ChannelCreating)
      expectMsg(ActorResponseOKMessage(channelCreator, Topic() :+ Communication :+ RabbitMQ, ChannelCreating))
      root ! ActorRequestMessage(Topic() :+ Communication :+ RabbitMQ :+ Database, ChannelCreating)
      expectMsgType[ActorResponseErrorMessage]
    }
    "respond with ActorResponseErrorMessage if an ActorRequestMessage having (topic <= Communication.RabbitMQ, service != ChannelCreating) is sento to it. Except for (topic = Comminication, service = Publishing)" in {
      root ! ActorRequestMessage(Topic() :+ Communication :+ RabbitMQ, Publishing)
      expectMsgType[ActorResponseErrorMessage]
      root ! ActorRequestMessage(Topic() :+ Communication, Master)
      expectMsgType[ActorResponseErrorMessage]
      root ! ActorRequestMessage(Topic() :+ Communication :+ RabbitMQ, UpdatesReceiving)
      expectMsgType[ActorResponseErrorMessage]
      root ! ActorRequestMessage(Topic() :+ Communication, NotificationSending)
      expectMsgType[ActorResponseErrorMessage]
      root ! ActorRequestMessage(Topic() :+ Communication, Publishing)
      expectMsg(ActorResponseOKMessage(publisherActor, Topic() :+ Communication, Publishing))
    }
  }

  val yellowPagerOne: ActorRef = system.actorOf(YellowPagesActor.topicProps(root,Topic() :+ Communication))
  val yellowPagerTwo: ActorRef = system.actorOf(YellowPagesActor.topicProps(root,Topic() :+ Communication :+ RabbitMQ))

  "Two yellow pages actors [(topic = Communication) and (topic = Communication.RabbitMQ)] have just been registered to the root yellow pages and, at this point, repeating the previous operations should still work" in {
    root ! ActorRequestMessage(Topic() :+ Communication, ChannelCreating)
    expectMsgType[ActorResponseErrorMessage]
    root ! ActorRequestMessage(Topic() :+ Communication :+ RabbitMQ, ChannelCreating)
    expectMsg(ActorResponseOKMessage(channelCreator, Topic() :+ Communication :+ RabbitMQ, ChannelCreating))
    root ! ActorRequestMessage(Topic() :+ Communication :+ RabbitMQ :+ Database, ChannelCreating)
    expectMsgType[ActorResponseErrorMessage]
    root ! ActorRequestMessage(Topic() :+ Communication :+ RabbitMQ, Publishing)
    expectMsgType[ActorResponseErrorMessage]
    root ! ActorRequestMessage(Topic() :+ Communication, Master)
    expectMsgType[ActorResponseErrorMessage]
    root ! ActorRequestMessage(Topic() :+ Communication :+ RabbitMQ, UpdatesReceiving)
    expectMsgType[ActorResponseErrorMessage]
    root ! ActorRequestMessage(Topic() :+ Communication, NotificationSending)
    expectMsgType[ActorResponseErrorMessage]
    root ! ActorRequestMessage(Topic() :+ Communication, Publishing)
    expectMsg(ActorResponseOKMessage(publisherActor, Topic() :+ Communication, Publishing))

  }

  "If the actors with (topic = General, service = Naming) and (topic = Communication.RabbitMQ, service = ChannelCreating) are deregistered from the root, their references should be recoverable any longer" in {
    root ! DeletionRequestMessage(naming, "Naming Actor", Topic() :+ General, Naming)
    root ! DeletionRequestMessage(channelCreator, "channelCreator", Topic() :+ Communication :+ RabbitMQ, ChannelCreating)
    root ! ActorRequestMessage(Topic() :+ General, Naming)
    expectMsgType[ActorResponseErrorMessage]
    root ! ActorRequestMessage(Topic() :+ Communication :+ RabbitMQ, ChannelCreating)
    expectMsgType[ActorResponseErrorMessage]

  }
}
