package org.gammf.collabora.database.actors

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit}
import com.newmotion.akka.rabbitmq.{ConnectionActor, ConnectionFactory}
import org.gammf.collabora.communication.actors._
import org.gammf.collabora.database.messages.{AskConnectionMessage, GetConnectionMessage, InsertNoteMessage}
import org.gammf.collabora.util.UpdateMessageImpl
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

import scala.concurrent.duration._

class DBActorTest extends TestKit (ActorSystem("CollaboraServer")) with WordSpecLike  with Matchers with BeforeAndAfterAll with ImplicitSender {

  val dbConnectionActor: ActorRef = system.actorOf(Props[ConnectionManagerActor])
  val factory = new ConnectionFactory()
  val connection:ActorRef = system.actorOf(ConnectionActor.props(factory), "rabbitmq")
  val naming: ActorRef = system.actorOf(Props[RabbitMQNamingActor], "naming")
  val channelCreator: ActorRef = system.actorOf(Props[ChannelCreatorActor], "channelCreator")
  val publisherActor: ActorRef = system.actorOf(Props[PublisherActor], "publisher")
  val notificationActor: ActorRef = system.actorOf(Props(new NotificationsSenderActor(connection, naming, channelCreator, publisherActor)))
  val dbActor: ActorRef = system.actorOf(Props.create(classOf[DBActor], dbConnectionActor, notificationActor))
  val subscriber:ActorRef = system.actorOf(Props[SubscriberActor], "subscriber")
  val updatesReceiver:ActorRef = system.actorOf(Props(
    new UpdatesReceiverActor(connection, naming, channelCreator, subscriber, dbActor)), "updates-receiver")

  override def afterAll(): Unit = {
    TestKit.shutdownActorSystem(system)
  }

  "A DB actor" should {
    "send back connection message correctly" in {
      within(500 millis) {
        dbConnectionActor ! new AskConnectionMessage()
        expectMsgType[GetConnectionMessage]
      }
    }

    "insert note in the db and after that, send a publish to NotificationActor" in {
      within(500 millis) {
        //dbActor ! new InsertNoteMessage()
      }
    }
  }

}

