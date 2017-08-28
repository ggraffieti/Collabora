package org.gammf.collabora

import akka.actor.{ActorSystem, Props}
import com.newmotion.akka.rabbitmq.{ConnectionActor, ConnectionFactory}
import org.gammf.collabora.communication.actors._
import org.gammf.collabora.communication.messages.StartMessage
import org.gammf.collabora.database.actors.{ConnectionManagerActor, DBMasterActor, DBWorkerNotesActor, PrintActor}
import us.raudi.pushraven.Pushraven

object EntryPoint extends App {

  private val AUTHORIZATION = "AAAAJtSw2Gk:APA91bEXmB5sRFqSnuYIP3qofHQ0RfHrAzTllJ0vYWtHXKZsMdbuXmUKbr16BVZsMO0cMmm_BWE8oLzkFcyuMr_V6O6ilqvLu7TrOgirVES51Ux9PsKfJ17iOMvTF_WtwqEURqMGBbLf"

  val system = ActorSystem("CollaboraServer")
  Pushraven.setKey(AUTHORIZATION)
  val factory = new ConnectionFactory()

  val rabbitConnection = system.actorOf(ConnectionActor.props(factory), "rabbitmq")
  val naming = system.actorOf(Props[RabbitMQNamingActor], "naming")
  val channelCreator = system.actorOf(Props[ChannelCreatorActor], "channelCreator")
  val publisherActor = system.actorOf(Props[PublisherActor], "publisher")

  val notificationActor = system.actorOf(Props(new NotificationsSenderActor(rabbitConnection, naming, channelCreator, publisherActor)))
  val collaborationActor = system.actorOf(Props(new CollaborationMembersActor(rabbitConnection, naming, channelCreator, publisherActor)))


  val dbMasterActor = system.actorOf(Props.create(classOf[DBMasterActor], system, notificationActor,collaborationActor))

  val subscriber = system.actorOf(Props[SubscriberActor], "subscriber")

  val updatesReceiver = system.actorOf(Props(
    new UpdatesReceiverActor(rabbitConnection, naming, channelCreator, subscriber, dbMasterActor)), "updates-receiver")

  updatesReceiver ! StartMessage
  notificationActor ! StartMessage
  collaborationActor ! StartMessage

}
