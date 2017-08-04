package org.gammf.collabora.communication

import akka.actor._
import com.newmotion.akka.rabbitmq._

/**
  * Created by mperuzzi on 03/08/17.
  */
class UpdatesReceiverActor(connection: ActorRef, naming: ActorRef, channelCreator: ActorRef,
                           subscriber: ActorRef) extends Actor {

  var subQueue: Option[String] = None

  override def receive: Receive = {
    case StartMessage => naming ! ChannelNamesRequestMessage(CommunicationType.UPDATES)
    case ChannelNamesResponseMessage(exchange, queue, routingKey) =>
      subQueue = queue
      channelCreator ! SubscribingChannelCreationMessage(connection, exchange, subQueue.get, routingKey)
    case ChannelCreatedMessage(channel) => subscriber ! SubscribeMessage(channel, subQueue.get)
    case _ => println("Huh?")
  }
}

object UseUpdatesReceiverActor extends App {
  implicit val system = ActorSystem()
  val factory = new ConnectionFactory()
  val connection = system.actorOf(ConnectionActor.props(factory), "rabbitmq")

  val naming = system.actorOf(Props[RabbitMQNamingActor], "naming")
  val channelCreator = system.actorOf(Props[ChannelCreatorActor], "channelCreator")
  val subscriber = system.actorOf(Props[SubscriberActor], "subscriber")
  val updatesReceiver = system.actorOf(Props(
    new UpdatesReceiverActor(connection, naming, channelCreator, subscriber)), "updates-receiver")

  updatesReceiver ! StartMessage
}
