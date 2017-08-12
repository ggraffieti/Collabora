package org.gammf.collabora

import akka.actor.{ActorSystem, Props}
import com.newmotion.akka.rabbitmq.{ConnectionActor, ConnectionFactory}
import org.gammf.collabora.communication.actors._
import org.gammf.collabora.communication.messages.StartMessage
import org.gammf.collabora.database.actors.{ConnectionManagerActor, DBWorkerNotesActor, PrintActor}

object Test extends App {
  val system = ActorSystem("CollaboraServer")

  val factory = new ConnectionFactory()

  val rabbitConnection = system.actorOf(ConnectionActor.props(factory), "rabbitmq")
  val naming = system.actorOf(Props[RabbitMQNamingActor], "naming")
  val channelCreator = system.actorOf(Props[ChannelCreatorActor], "channelCreator")
  val publisherActor = system.actorOf(Props[PublisherActor], "publisher")

  val notificationActor = system.actorOf(Props(new NotificationsSenderActor(rabbitConnection, naming, channelCreator, publisherActor)))

  val dbConnectionActor = system.actorOf(Props[ConnectionManagerActor])
  val dbActor = system.actorOf(Props.create(classOf[DBWorkerNotesActor], dbConnectionActor, notificationActor))

  val subscriber = system.actorOf(Props[SubscriberActor], "subscriber")

  val updatesReceiver = system.actorOf(Props(
    new UpdatesReceiverActor(rabbitConnection, naming, channelCreator, subscriber, dbActor)), "updates-receiver")

  updatesReceiver ! StartMessage
  notificationActor ! StartMessage

}
