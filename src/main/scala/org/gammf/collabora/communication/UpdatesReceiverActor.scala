package org.gammf.collabora.communication

import akka.actor._
import com.newmotion.akka.rabbitmq._

/**
  * Created by mperuzzi on 03/08/17.
  */
class UpdatesReceiverActor(connection: ActorRef, channelCreator: ActorRef, subscriber: ActorRef) extends Actor {

  val exchange = "notifications"
  val queue = "notify.username"
  val routingKey = None

  override def receive: Receive = {
    case StartMessage => channelCreator ! SubscribingChannelCreationMessage(
      connection, exchange, queue, routingKey)
    case ChannelCreatedMessage(channel) => subscriber ! SubscribeMessage(channel, queue)
    case _ => println("Huh?")
  }
}

object UseUpdatesReceiverActor extends App {
  implicit val system = ActorSystem()
  val factory = new ConnectionFactory()
  val connection = system.actorOf(ConnectionActor.props(factory), "rabbitmq")

  val channelCreator = system.actorOf(Props[ChannelCreatorActor], "channelCreator")
  val subscriber = system.actorOf(Props[SubscriberActor], "subscriber")
  val updatesReceiver = system.actorOf(Props(
    new UpdatesReceiverActor(connection, channelCreator, subscriber)), "updates-receiver")

  updatesReceiver ! StartMessage
}
