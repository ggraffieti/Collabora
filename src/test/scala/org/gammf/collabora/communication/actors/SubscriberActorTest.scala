package org.gammf.collabora.communication.actors

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.testkit.{DefaultTimeout, ImplicitSender, TestKit}
import com.newmotion.akka.rabbitmq.{Channel, ConnectionActor, ConnectionFactory}
import com.rabbitmq.client.{Channel, Connection, ConnectionFactory}
import org.gammf.collabora.{TestMessageUtil, TestUtil}
import org.gammf.collabora.communication.messages._
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}


class SubscriberActorTest extends TestKit (ActorSystem("CollaboraServer")) with WordSpecLike with DefaultTimeout with Matchers with BeforeAndAfterAll with ImplicitSender {

/*
  val CONNECTION_ACTOR_NAME = "rabbitmq"
  val CHANNEL_CREATOR_NAME = "channelCreator"
  val SUBSCRIBER_ACTOR_NAME = "subscriber"

  private val EXCHANGE_NAME = "updates"
  private val ROUTING_KEY = ""
>>>>>>> e1352d43aebaf97ca96e951fc473704c444d2b97

  val factory = new ConnectionFactory()
  val connection:ActorRef = system.actorOf(ConnectionActor.props(factory), CONNECTION_ACTOR_NAME)
  val channelCreator: ActorRef = system.actorOf(Props[ChannelCreatorActor], CHANNEL_CREATOR_NAME)
  val subscriber:ActorRef = system.actorOf(Props[SubscriberActor], SUBSCRIBER_ACTOR_NAME)

  val connectemp: Connection = factory.newConnection
  var channel: Channel = connectemp.createChannel

  override def afterAll(): Unit = {
    TestKit.shutdownActorSystem(system)
  }


  "A Subscriber actor" should {

    "subscribes on a certain queue in a rabbitMQ channel correctly" in {
      channelCreator ! SubscribingChannelCreationMessage(connection, TestUtil.TYPE_UPDATES, TestUtil.SERVER_UPDATE, None)
      val ChannelCreatedMessage(channel) = expectMsgType[ChannelCreatedMessage]
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

  }*/
}
