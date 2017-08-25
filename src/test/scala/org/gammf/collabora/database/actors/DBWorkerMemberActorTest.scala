package org.gammf.collabora.database.actors

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit}
import com.newmotion.akka.rabbitmq.{ConnectionActor, ConnectionFactory}
import org.gammf.collabora.communication.actors._
import org.gammf.collabora.database.messages._
import org.gammf.collabora.util.{CollaborationRight, CollaborationUser}
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

import scala.concurrent.duration._

class DBWorkerMemberActorTest extends TestKit (ActorSystem("CollaboraServer")) with WordSpecLike  with Matchers with BeforeAndAfterAll with ImplicitSender {

  val factory = new ConnectionFactory()
  val connection:ActorRef = system.actorOf(ConnectionActor.props(factory), "rabbitmq")
  val naming:ActorRef = system.actorOf(Props[RabbitMQNamingActor], "naming")
  val channelCreator :ActorRef= system.actorOf(Props[ChannelCreatorActor], "channelCreator")
  val publisherActor:ActorRef = system.actorOf(Props[PublisherActor], "publisher")
  val notificationActor:ActorRef = system.actorOf(Props(new NotificationsSenderActor(connection, naming, channelCreator, publisherActor)))
  val dbConnectionActor :ActorRef= system.actorOf(Props[ConnectionManagerActor])
  val dbMasterActor:ActorRef = system.actorOf(Props.create(classOf[DBMasterActor], system, notificationActor))
  val connectionManagerActor: ActorRef =  system.actorOf(Props[ConnectionManagerActor])
  val usersActor:ActorRef = system.actorOf(Props.create(classOf[DBWorkerMemberActor], connectionManagerActor))

  val user = CollaborationUser("peru", CollaborationRight.WRITE)

  override def beforeAll(): Unit = {

  }

  override def afterAll(): Unit = {
    TestKit.shutdownActorSystem(system)
  }

  "A DBWorkerMember actor" should {
    "insert new user in a collaboration correctly in the db" in {
      within(3 second) {
        usersActor ! InsertUserMessage(user, "59806a4af27da3fcfe0ac0ca", "maffone",None)
        expectMsgType[QueryOkMessage]
      }
    }

    "update a user right in a collaboration correctly" in {
      within(1 second) {
        usersActor ! UpdateUserMessage(user, "59806a4af27da3fcfe0ac0ca", "maffone")
        expectMsgType[QueryOkMessage]
      }
    }

    "delete a user in a collaboration correctly" in {
      within(1 second) {
        usersActor ! DeleteUserMessage(user, "59806a4af27da3fcfe0ac0ca", "maffone")
        expectMsgType[QueryOkMessage]
      }
    }



  }
}

