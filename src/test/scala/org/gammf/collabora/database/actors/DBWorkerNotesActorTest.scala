package org.gammf.collabora.database.actors

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit}
import com.newmotion.akka.rabbitmq.{ConnectionActor, ConnectionFactory}
import org.gammf.collabora.communication.actors._
import org.gammf.collabora.database.messages._
import org.gammf.collabora.util.{Note, NoteState, SimpleNote}
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}
import scala.concurrent.duration._

class DBWorkerNotesActorTest extends TestKit (ActorSystem("CollaboraServer")) with WordSpecLike  with Matchers with BeforeAndAfterAll with ImplicitSender {

  val factory = new ConnectionFactory()
  val connection:ActorRef = system.actorOf(ConnectionActor.props(factory), "rabbitmq")
  val naming:ActorRef = system.actorOf(Props[RabbitMQNamingActor], "naming")
  val channelCreator :ActorRef= system.actorOf(Props[ChannelCreatorActor], "channelCreator")
  val publisherActor:ActorRef = system.actorOf(Props[PublisherActor], "publisher")
  val notificationActor:ActorRef = system.actorOf(Props(new NotificationsSenderActor(connection, naming, channelCreator, publisherActor)))
  val dbConnectionActor :ActorRef= system.actorOf(Props[ConnectionManagerActor])
  val dbMasterActor:ActorRef = system.actorOf(Props.create(classOf[DBMasterActor], system, notificationActor))
  val connectionManagerActor: ActorRef =  system.actorOf(Props[ConnectionManagerActor])
  val notesActor:ActorRef = system.actorOf(Props.create(classOf[DBWorkerNotesActor], connectionManagerActor))
  val noteId:String = "123456788354670000000000"

  val notetmp:Note = SimpleNote(Option(noteId), "prova test", None,
    None, None, new NoteState("done",None), None)

  override def beforeAll(): Unit = {

  }

  override def afterAll(): Unit = {
    TestKit.shutdownActorSystem(system)
  }

  "A DBWorkerNotes actor" should {
    "insert new notes correctly in the db" in {

      within(1 second) {
        notesActor ! InsertNoteMessage(notetmp, "59806a4af27da3fcfe0ac0ca", "maffone")
        expectMsgType[QueryOkMessage]
      }
    }

    "update notes correctly" in {
      within(1 second) {
        notesActor ! UpdateNoteMessage(notetmp, "59806a4af27da3fcfe0ac0ca", "maffone")
        expectMsgType[QueryOkMessage]
      }
    }

    "delete notes correctly" in {
      within(1 second) {
        notesActor ! DeleteNoteMessage(notetmp, "59806a4af27da3fcfe0ac0ca", "maffone")
        expectMsgType[QueryOkMessage]
      }
    }



  }
}

