package org.gammf.collabora.communication

import akka.actor._
import com.newmotion.akka.rabbitmq._

/**
  * @author Manuel Peruzzi
  * This is an actor that manages the reception of client updates.
  */
class UpdatesReceiverActor(connection: ActorRef, naming: ActorRef, channelCreator: ActorRef,
                           subscriber: ActorRef) extends Actor {

  private var subQueue: Option[String] = None

  override def receive: Receive = {
    case StartMessage => naming ! ChannelNamesRequestMessage(CommunicationType.UPDATES)
    case ChannelNamesResponseMessage(exchange, queue, routingKey) =>
      subQueue = queue
      channelCreator ! SubscribingChannelCreationMessage(connection, exchange, subQueue.get, routingKey)
    case ChannelCreatedMessage(channel) => subscriber ! SubscribeMessage(channel, subQueue.get)
    case ClientUpdateMessage(text) => println("[Updates Receiver Actor] Received: " + text)
    case _ => println("[Updates Receiver Actor] Huh?")
  }
}

/**
  * This is a simple application that uses the Updates Receiver Actor
  */
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
