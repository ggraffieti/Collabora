package org.gammf.collabora.database.actors

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit}
import com.newmotion.akka.rabbitmq.{ConnectionActor, ConnectionFactory}
import org.gammf.collabora.Test._
import org.gammf.collabora.communication.actors._
import org.gammf.collabora.communication.messages.{ClientUpdateMessage, StartMessage}
import org.gammf.collabora.database.messages._
import org.gammf.collabora.util.{Collaboration, CollaborationRight, CollaborationType, CollaborationUser, Location, Module, NoteState, SimpleCollaboration, SimpleModule, SimpleNote}
import org.joda.time.DateTime
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

import scala.concurrent.duration._

class DBWorkerCollaborationsActorTest extends TestKit (ActorSystem("CollaboraServer")) with WordSpecLike  with Matchers with BeforeAndAfterAll with ImplicitSender {

  val factory = new ConnectionFactory()
  val connection:ActorRef = system.actorOf(ConnectionActor.props(factory), "rabbitmq")
  val naming:ActorRef = system.actorOf(Props[RabbitMQNamingActor], "naming")
  val channelCreator :ActorRef= system.actorOf(Props[ChannelCreatorActor], "channelCreator")
  val publisherActor:ActorRef = system.actorOf(Props[PublisherActor], "publisher")
  val notificationActor:ActorRef = system.actorOf(Props(new NotificationsSenderActor(connection, naming, channelCreator, publisherActor)))
  val dbConnectionActor :ActorRef= system.actorOf(Props[ConnectionManagerActor])
  val collaborationMemberActor:ActorRef = system.actorOf(Props(new CollaborationMembersActor(connection, naming, channelCreator, publisherActor)))
  val dbMasterActor:ActorRef = system.actorOf(Props.create(classOf[DBMasterActor], system, notificationActor,collaborationMemberActor))
  val connectionManagerActor: ActorRef =  system.actorOf(Props[ConnectionManagerActor])
  val collaborationsActor:ActorRef = system.actorOf(Props.create(classOf[DBWorkerCollaborationsActor], connectionManagerActor))
  val collabID:String = "123456788698540008123400"

  val collab:Collaboration = SimpleCollaboration(Option(collabID),"simplecollaboration",
    CollaborationType.GROUP,
    Option(List(CollaborationUser("fone", CollaborationRight.ADMIN), CollaborationUser("peru", CollaborationRight.ADMIN))),
    Option.empty,
    Option(List(SimpleNote(Option("prova"),"questo è il contenuto",Option(new DateTime()),Option(Location(23.32,23.42)),Option.empty,NoteState("doing", Option("fone")),Option.empty),
      SimpleNote(Option("prova2"),"questo è il contenuto2",Option(new DateTime()),Option(Location(233.32,233.42)),Option.empty,NoteState("done", Option("peru")),Option.empty))))

  override def beforeAll(): Unit = {

  }

  override def afterAll(): Unit = {
    TestKit.shutdownActorSystem(system)
  }

  "A DBWorkerCollaborations actor" should {
    "insert new collaboration in the db" in {

      within(3 second) {
        collaborationsActor ! InsertCollaborationMessage(collab, "maffone")
        expectMsgType[QueryOkMessage]
      }
    }

    "update a collaboration in the db" in {
      within(1 second) {
        collaborationsActor ! UpdateCollaborationMessage(collab, "maffone")
        expectMsgType[QueryOkMessage]
      }
    }

    "delete a collaboration in the db" in {
      within(1 second) {
        collaborationsActor ! DeleteCollaborationMessage(collab, "maffone")
        expectMsgType[QueryOkMessage]
      }
    }



  }
}
