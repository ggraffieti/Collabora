package org.gammf.collabora.communication.actors

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.testkit.{DefaultTimeout, ImplicitSender, TestKit}
import com.newmotion.akka.rabbitmq.{Channel, ConnectionActor, ConnectionFactory}
import com.rabbitmq.client.{Channel, Connection, ConnectionFactory}
import org.gammf.collabora.communication.messages._
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}


class SubscriberActorTest extends TestKit (ActorSystem("CollaboraServer")) with WordSpecLike with DefaultTimeout with Matchers with BeforeAndAfterAll with ImplicitSender {

  private val EXCHANGE_NAME = "updates"
  private val ROUTING_KEY = ""

  val factory = new ConnectionFactory()
  val connection:ActorRef = system.actorOf(ConnectionActor.props(factory), "rabbitmq")
  val channelCreator: ActorRef = system.actorOf(Props[ChannelCreatorActor], "channelCreator")
  val subscriber:ActorRef = system.actorOf(Props[SubscriberActor], "subscriber")

  val connectemp: Connection = factory.newConnection
  var channel: Channel = connectemp.createChannel

  override def afterAll(): Unit = {
    TestKit.shutdownActorSystem(system)
  }


  "A Subscriber actor" should {

    "subscribes on a certain queue in a rabbitMQ channel correctly" in {
      channelCreator ! SubscribingChannelCreationMessage(connection, "updates", "update.server", None)
      val ChannelCreatedMessage(channel) = expectMsgType[ChannelCreatedMessage]
      subscriber ! SubscribeMessage(channel, "update.server")
      this.channel = channel
      val message = "{\"messageType\": \"insertion\",\"target\" : \"note\",\"user\" : \"maffone\",\"note\": {\"content\" : \"setup working enviroment\",\"expiration\" : \"2017-08-07T06:01:17.171Z\",\"location\" : { \"latitude\" : 546, \"longitude\" : 324 },\"previousNotes\" : [ \"5980710df27da3fcfe0ac88e\", \"5980710df27da3fcfe0ac88f\" ],\"state\" : { \"definition\" : \"done\", \"username\" : \"maffone\"}}}"
      channel.basicPublish(EXCHANGE_NAME, ROUTING_KEY, null, message.getBytes("UTF-8"))
      expectMsg(ClientUpdateMessage("{\"messageType\": \"insertion\",\"target\" : \"note\",\"user\" : \"maffone\",\"note\": {\"content\" : \"setup working enviroment\",\"expiration\" : \"2017-08-07T06:01:17.171Z\",\"location\" : { \"latitude\" : 546, \"longitude\" : 324 },\"previousNotes\" : [ \"5980710df27da3fcfe0ac88e\", \"5980710df27da3fcfe0ac88f\" ],\"state\" : { \"definition\" : \"done\", \"username\" : \"maffone\"}}}"))
    }

    "capturing all the messages send to setted queue " in {
      for(_ <- 1 to 5){
        val message = "{\"messageType\": \"insertion\",\"target\" : \"note\",\"user\" : \"maffone\",\"note\": {\"content\" : \"setup working enviroment\",\"expiration\" : \"2017-08-07T06:01:17.171Z\",\"location\" : { \"latitude\" : 546, \"longitude\" : 324 },\"previousNotes\" : [ \"5980710df27da3fcfe0ac88e\", \"5980710df27da3fcfe0ac88f\" ],\"state\" : { \"definition\" : \"done\", \"username\" : \"maffone\"}}}"
        channel.basicPublish(EXCHANGE_NAME, ROUTING_KEY, null, message.getBytes("UTF-8"))
      }
      var messages = Seq[ClientUpdateMessage]()
      receiveWhile(){
        case msg:ClientUpdateMessage => messages = msg +: messages
      }
      messages.length should be(5)
    }

  }
}
