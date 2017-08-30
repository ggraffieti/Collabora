package org.gammf.collabora

import akka.actor.{ActorSystem, Props}
import com.newmotion.akka.rabbitmq.{ConnectionActor, ConnectionFactory}
import org.gammf.collabora.authentication.AuthenticationServer
import org.gammf.collabora.communication.actors._
import org.gammf.collabora.communication.messages.StartMessage
import org.gammf.collabora.database.actors.DBMasterActor

object EntryPoint extends App {
  val system = ActorSystem("CollaboraServer")


  val factory = new ConnectionFactory()
  val rabbitConnection = system.actorOf(ConnectionActor.props(factory), "rabbitmq")
  val naming = system.actorOf(Props[RabbitMQNamingActor], "naming")
  val channelCreator = system.actorOf(Props[ChannelCreatorActor], "channelCreator")
  val publisherActor = system.actorOf(Props[PublisherActor], "publisher")
  val collaborationActor = system.actorOf(Props(new CollaborationMembersActor(rabbitConnection, naming, channelCreator, publisherActor)))
  val notificationActor = system.actorOf(Props(new NotificationsSenderActor(rabbitConnection, naming, channelCreator, publisherActor,system)))
  val dbMasterActor = system.actorOf(Props.create(classOf[DBMasterActor], system, notificationActor,collaborationActor))
  val subscriber = system.actorOf(Props[SubscriberActor], "subscriber")
  val updatesReceiver = system.actorOf(Props(
    new UpdatesReceiverActor(rabbitConnection, naming, channelCreator, subscriber, dbMasterActor)), "updates-receiver")

  //AuthenticationServer.start(system, dbMasterActor)


  updatesReceiver ! StartMessage
  notificationActor ! StartMessage
  collaborationActor ! StartMessage

}
