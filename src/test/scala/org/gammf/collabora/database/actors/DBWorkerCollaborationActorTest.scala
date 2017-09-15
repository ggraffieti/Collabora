package org.gammf.collabora.database.actors

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit}
import com.newmotion.akka.rabbitmq.{ConnectionActor, ConnectionFactory}
import org.gammf.collabora.communication.actors._
import org.gammf.collabora.database.actors.master.DBMasterActor
import org.gammf.collabora.database.actors.worker.DBWorkerCollaborationActor
import org.gammf.collabora.database.messages._
import org.gammf.collabora.util.{Collaboration, CollaborationRight, CollaborationType, CollaborationUser, Location, Module, NoteState, SimpleCollaboration, SimpleModule, SimpleNote}
import org.joda.time.DateTime
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

import scala.concurrent.duration._

class DBWorkerCollaborationActorTest extends TestKit (ActorSystem("CollaboraServer")) with WordSpecLike  with Matchers with BeforeAndAfterAll with ImplicitSender {

  /*val factory = new ConnectionFactory()
  val connection:ActorRef = system.actorOf(ConnectionActor.props(factory), "rabbitmq")
  val naming:ActorRef = system.actorOf(Props[RabbitMQNamingActor], "naming")
  val channelCreator :ActorRef= system.actorOf(Props[ChannelCreatorActor], "channelCreator")
  val publisherActor:ActorRef = system.actorOf(Props[PublisherActor], "publisher")
  val collaborationMemberActor:ActorRef = system.actorOf(Props(new CollaborationMembersActor(connection, naming, channelCreator, publisherActor)))
  val notificationActor:ActorRef = system.actorOf(Props(new NotificationsSenderActor(connection, naming, channelCreator, publisherActor,system)))
  val dbConnectionActor :ActorRef= system.actorOf(Props[ConnectionManagerActor])
  val dbMasterActor:ActorRef = system.actorOf(Props.create(classOf[DBMasterActor], system, notificationActor,collaborationMemberActor))
  val connectionManagerActor: ActorRef =  system.actorOf(Props[ConnectionManagerActor])
  val collaborationsActor:ActorRef = system.actorOf(Props.create(classOf[DBWorkerCollaborationsActor], connectionManagerActor))
  val collabID:String = "123456788698540008123400"

  val collab:Collaboration = SimpleCollaboration(
    id = Some(collabID),
    name = "simplecollaboration",
    collaborationType = CollaborationType.GROUP,
    users = Some(List(CollaborationUser("fone", CollaborationRight.ADMIN), CollaborationUser("peru", CollaborationRight.ADMIN))),
    modules = Option.empty,
    notes = Some(List(SimpleNote(None, "questo è il contenuto",Some(new DateTime()),Some(Location(23.32,23.42)),Option.empty,NoteState("doing", Some("fone")),None),
      SimpleNote(None,"questo è il contenuto2",Some(new DateTime()),Some(Location(233.32,233.42)),None,NoteState("done", Option("peru")),None)))
  )

  override def beforeAll(): Unit = {

  }

  override def afterAll(): Unit = {
    TestKit.shutdownActorSystem(system)
  }

  "A DBWorkerCollaborations actor" should {
    "insert new collaboration in the db" in {

      within(5 second) {
        collaborationsActor ! InsertCollaborationMessage(collab, "maffone")
        expectMsgType[QueryOkMessage]
      }
    }

    "update a collaboration in the db" in {
      within(5 second) {
        collaborationsActor ! UpdateCollaborationMessage(collab, "maffone")
        expectMsgType[QueryOkMessage]
      }
    }

    "delete a collaboration in the db" in {
      within(5 second) {
        collaborationsActor ! DeleteCollaborationMessage(collab, "maffone")
        expectMsgType[QueryOkMessage]
      }
    }
  }*/
}
