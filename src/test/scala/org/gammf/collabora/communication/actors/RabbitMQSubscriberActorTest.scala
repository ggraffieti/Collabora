package org.gammf.collabora.communication.actors

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.pattern.ask
import akka.testkit.{DefaultTimeout, ImplicitSender, TestKit}
import akka.util.Timeout
import com.newmotion.akka.rabbitmq.{Channel, ConnectionActor, ConnectionFactory}
import com.rabbitmq.client.{Channel, Connection, ConnectionFactory}
import org.gammf.collabora.{TestMessageUtil, TestUtil}
import org.gammf.collabora.communication.messages._
import org.gammf.collabora.yellowpages.ActorContainer
import org.gammf.collabora.yellowpages.ActorService.{ChannelCreating, ConnectionHandler}
import org.gammf.collabora.yellowpages.actors.YellowPagesActor
import org.gammf.collabora.yellowpages.messages._
import org.gammf.collabora.yellowpages.util.Topic
import org.gammf.collabora.yellowpages.TopicElement._
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

class RabbitMQSubscriberActorTest extends TestKit (ActorSystem("CollaboraTest")) with WordSpecLike with DefaultTimeout with Matchers with BeforeAndAfterAll with ImplicitSender {


  override def afterAll(): Unit = {
    TestKit.shutdownActorSystem(system)
  }
/*
  private val EXCHANGE_NAME = "updates"
  private val ROUTING_KEY = ""
  /*
  val CONNECTION_ACTOR_NAME = "RabbitConnection"
  val CHANNEL_CREATOR_NAME = "RabbitChannelCreator"
  val SUBSCRIBER_ACTOR_NAME = "SubscriberActor"

  val actorCreator = new ActorCreator(system)
  val rootYellowPages = actorCreator.getYellowPagesRoot

  val factory = new ConnectionFactory()
  val rabbitConnection = system.actorOf(ConnectionActor.props(factory), CONNECTION_ACTOR_NAME)
  rootYellowPages ! RegistrationRequestMessage(rabbitConnection, CONNECTION_ACTOR_NAME, Topic() :+ Communication :+ RabbitMQ, ConnectionHandler)

  val channelCreator = system.actorOf(ChannelCreatorActor.channelCreatorProps(rootYellowPages, Topic() :+ Communication :+ RabbitMQ, CHANNEL_CREATOR_NAME))
  val subscriber = system.actorOf(SubscriberActor.subscriberProps(rootYellowPages, Topic() :+ Communication :+ RabbitMQ, SUBSCRIBER_ACTOR_NAME))
*/

  val factory = new ConnectionFactory()
  val connectemp: Connection = factory.newConnection
  var channel: Channel = connectemp.createChannel

  implicit protected[this] val askTimeout: Timeout = Timeout(5 second)

  val actorCreator = new ActorCreator(system)
  actorCreator.startCreation
  val rootYellowPages = actorCreator.getYellowPagesRoot

  override def afterAll(): Unit = {
    TestKit.shutdownActorSystem(system)
  }


  "A Subscriber actor" should {

    "subscribes on a certain queue in a rabbitMQ channel correctly" in {
     /* (rootYellowPages ? ActorRequestMessage(Topic() :+ Communication :+ RabbitMQ, ChannelCreating))
        .mapTo[ActorResponseMessage].map {
        case response: ActorResponseOKMessage => response.actor ! SubscribingChannelCreationMessage(TestUtil.TYPE_UPDATES, TestUtil.SERVER_UPDATE, None)
        case _ =>

          expectMsgType[RegistrationResponseMessage]
      }
      */
      channelCreator ! SubscribingChannelCreationMessage(TestUtil.TYPE_UPDATES, TestUtil.SERVER_UPDATE, None)
      val ChannelCreatedMessage(channel) = expectMsgType[RegistrationResponseMessage]

      subscriber ! SubscribeMessage(channel, TestUtil.SERVER_UPDATE)
      this.channel = channel
      val message = TestMessageUtil.messageSubscriberActorTest
      channel.basicPublish(TestUtil.TYPE_UPDATES, TestUtil.ROUTING_KEY_EMPTY, null, message.getBytes(TestUtil.STRING_ENCODING))
      expectMsg(ClientUpdateMessage(TestMessageUtil.messageSubscriberActorTest))
    }

    "capturing all the messages send to setted queue " in {
      for(_ <- TestUtil.START_FOR_INDEX to TestUtil.FINAL_FOR_INDEX){
        val message = TestMessageUtil.messageSubscriberActorTest
        channel.basicPublish(TestUtil.TYPE_UPDATES, TestUtil.ROUTING_KEY_EMPTY, null, message.getBytes(TestUtil.STRING_ENCODING))
      }
      var messages = Seq[ClientUpdateMessage]()
      receiveWhile(){
        case msg:ClientUpdateMessage => messages = msg +: messages
      }
      messages.length should be(TestUtil.MESSAGE_LENGTH)
    }

  }
*/
}
