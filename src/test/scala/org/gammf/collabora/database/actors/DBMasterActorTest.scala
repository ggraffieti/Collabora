package org.gammf.collabora.database.actors

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import akka.pattern.ask
import akka.testkit.{ImplicitSender, TestKit}
import akka.util.Timeout
import org.gammf.collabora.communication.messages.PublishErrorMessageInCollaborationExchange
import org.gammf.collabora.{TestMessageUtil, TestUtil}
import org.gammf.collabora.database.messages.{GetAllCollaborationsMessage, GetCollaborationMessage, QueryFailMessage}
import org.gammf.collabora.util.{Collaboration, CollaborationUser, Module, Note, ServerErrorCode, ServerErrorMessage, UpdateMessage}
import org.gammf.collabora.yellowpages.{ActorContainer, TopicElement}
import org.gammf.collabora.yellowpages.ActorService._
import org.gammf.collabora.yellowpages.messages._
import org.gammf.collabora.yellowpages.util.Topic
import org.gammf.collabora.yellowpages.TopicElement._
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

import scala.concurrent.duration._
import org.scalatest.concurrent.Eventually

import scala.concurrent.Await
import scala.language.postfixOps


class DBMasterActorTest extends TestKit (ActorSystem("CollaboraTest")) with WordSpecLike with Eventually with Matchers with BeforeAndAfterAll with ImplicitSender {

  var expectedNote: Option[Note] = None
  var expectedModule: Option[Module] = None
  var expectedCollaboration: Option[Collaboration] = None
  var expectedCollaborationString: String = ""
  var expectedCollaborationsString: String = ""
  var expectedMember: Option[CollaborationUser] = None
  var expectedDBError: Option[ServerErrorMessage] = None

  implicit protected[this] val askTimeout: Timeout = Timeout(5 second)
  var rootYellowPages: ActorRef = _

  override def beforeAll(): Unit = {
    ActorContainer.init()
    ActorContainer.createAll()
    rootYellowPages = ActorContainer.rootYellowPages

    deregisterDBMasters()
    registerFakeDBMasters()

    deregisterCollaborationMemberActor()
    registerFakeCollaborationMemberActor()

    Thread.sleep(200)
  }
  override def afterAll(): Unit = {
    ActorContainer.shutdown()
    TestKit.shutdownActorSystem(system)
  }

  override implicit val patienceConfig: PatienceConfig = PatienceConfig(
    timeout = scaled(TestUtil.TASK_WAIT_TIME seconds),
    interval = scaled(TestUtil.INTERVAL_MILLIS millis)
  )

  "A DBMaster actor" should {

    "act like a gateway for every request to the DB" in {
     Await.result(rootYellowPages ? ActorRequestMessage(Topic() :+ Database, Master), askTimeout.duration)
       .asInstanceOf[ActorResponseMessage] match {
       case response: ActorResponseOKMessage =>
         response.actor ! TestMessageUtil.noteUpdateMessage
       case _ => fail

     }
     eventually {
       expectedNote should not be None
     }
     assert(expectedNote.get == TestMessageUtil.notificationNote)

     Await.result(rootYellowPages ? ActorRequestMessage(Topic() :+ Database, Master), askTimeout.duration)
       .asInstanceOf[ActorResponseMessage] match {
       case response: ActorResponseOKMessage =>
         response.actor ! TestMessageUtil.moduleUpdateMessage
       case _ => fail

     }
     eventually {
       expectedModule should not be None
     }
     assert(expectedModule.get == TestMessageUtil.notificationModule)

     Await.result(rootYellowPages ? ActorRequestMessage(Topic() :+ Database, Master), askTimeout.duration)
       .asInstanceOf[ActorResponseMessage] match {
       case response: ActorResponseOKMessage =>
         response.actor ! TestMessageUtil.collaborationUpdateMessage
       case _ => fail

     }
     eventually {
       expectedCollaboration should not be None
     }
     assert(expectedCollaboration.get == TestMessageUtil.notificationCollaboration)

     Await.result(rootYellowPages ? ActorRequestMessage(Topic() :+ Database, Master), askTimeout.duration)
       .asInstanceOf[ActorResponseMessage] match {
       case response: ActorResponseOKMessage =>
         response.actor ! TestMessageUtil.memberUpdateMessage
       case _ => fail

     }
     eventually {
       expectedMember should not be None
     }
     assert(expectedMember.get == TestMessageUtil.notificationMember)

     Await.result(rootYellowPages ? ActorRequestMessage(Topic() :+ Database, Master), askTimeout.duration)
       .asInstanceOf[ActorResponseMessage] match {
       case response: ActorResponseOKMessage =>
         response.actor ! GetCollaborationMessage("1234567890")
       case _ => fail

     }
     eventually {
       expectedCollaborationString should not be ""
     }
     assert(expectedCollaborationString == "1234567890")

     Await.result(rootYellowPages ? ActorRequestMessage(Topic() :+ Database, Master), askTimeout.duration)
       .asInstanceOf[ActorResponseMessage] match {
       case response: ActorResponseOKMessage =>
         response.actor ! GetAllCollaborationsMessage("fone")
       case _ => fail

     }
     eventually {
       expectedCollaborationsString should not be ""
     }
     assert(expectedCollaborationsString == "fone")
   }

    "contact the CollaborationMember actor whenever a QueryFail message is received" in {
      Await.result(rootYellowPages ? ActorRequestMessage(Topic() :+ Database, Master), askTimeout.duration)
        .asInstanceOf[ActorResponseMessage] match {
        case response: ActorResponseOKMessage =>
          response.actor ! QueryFailMessage(new Exception, "fone")
        case _ => fail

      }
      eventually {
        expectedDBError should not be None
      }
      assert(expectedDBError.get == ServerErrorMessage("fone", ServerErrorCode.SERVER_ERROR))
    }
  }

  private def deregisterDBMasters():Unit = {
    Thread.sleep(200)
    Await.result(rootYellowPages ? ActorRequestMessage(Topic() :+ Database :+ TopicElement.Note, Master), askTimeout.duration)
      .asInstanceOf[ActorResponseMessage] match {
      case response: ActorResponseOKMessage => rootYellowPages ! DeletionRequestMessage(response.actor, "DBMasterNotes", Topic() :+ Database :+ TopicElement.Note, Master)
      case _ => fail
    }

    Await.result(rootYellowPages ? ActorRequestMessage(Topic() :+ Database :+ TopicElement.Module, Master), askTimeout.duration)
      .asInstanceOf[ActorResponseMessage] match {
      case response: ActorResponseOKMessage => rootYellowPages ! DeletionRequestMessage(response.actor, "DBMasterModules", Topic() :+ Database :+ TopicElement.Module, Master)
      case _ => fail
    }

    Await.result(rootYellowPages ? ActorRequestMessage(Topic() :+ Database :+ TopicElement.Collaboration, Master), askTimeout.duration)
      .asInstanceOf[ActorResponseMessage] match {
      case response: ActorResponseOKMessage => rootYellowPages ! DeletionRequestMessage(response.actor, "DBMasterCollaborations", Topic() :+ Database :+ TopicElement.Collaboration, Master)
      case _ => fail
    }

    Await.result(rootYellowPages ? ActorRequestMessage(Topic() :+ Database :+ Member, Master), askTimeout.duration)
      .asInstanceOf[ActorResponseMessage] match {
      case response: ActorResponseOKMessage => rootYellowPages ! DeletionRequestMessage(response.actor, "DBMasterMembers", Topic() :+ Database :+ Member, Master)
      case _ => fail
    }
  }

  private def registerFakeDBMasters():Unit = {
    val fakeDBNoteMaster = ActorContainer.actorSystem.actorOf(Props(new Actor {
      override def receive: Receive = {
        case message: UpdateMessage => expectedNote = message.note
      }
    }))
    rootYellowPages ! RegistrationRequestMessage(fakeDBNoteMaster, "fakeDBNoteMaster", Topic() :+ Database :+ TopicElement.Note, Master)

    val fakeDBModuleMaster = ActorContainer.actorSystem.actorOf(Props(new Actor {
      override def receive: Receive = {
        case message: UpdateMessage => expectedModule = message.module
      }
    }))
    rootYellowPages ! RegistrationRequestMessage(fakeDBModuleMaster, "fakeDBModuleMaster", Topic() :+ Database :+ TopicElement.Module, Master)

    val fakeDBCollaborationMaster = ActorContainer.actorSystem.actorOf(Props(new Actor {
      override def receive: Receive = {
        case message: UpdateMessage => expectedCollaboration = message.collaboration
        case message: GetAllCollaborationsMessage =>expectedCollaborationsString = message.username
        case message: GetCollaborationMessage => expectedCollaborationString = message.collaborationID
      }
    }))
    rootYellowPages ! RegistrationRequestMessage(fakeDBCollaborationMaster, "fakeDBCollaborationMaster", Topic() :+ Database :+ TopicElement.Collaboration, Master)

    val fakeDBMemberMaster = ActorContainer.actorSystem.actorOf(Props(new Actor {
      override def receive: Receive = {
        case message: UpdateMessage => expectedMember = message.member
      }
    }))
    rootYellowPages ! RegistrationRequestMessage(fakeDBMemberMaster, "fakeDBMemberMaster", Topic() :+ Database :+ TopicElement.Member, Master)

  }

  private def deregisterCollaborationMemberActor(): Unit = {
    Await.result(rootYellowPages ? ActorRequestMessage(Topic() :+ Communication :+ Collaborations :+ RabbitMQ, Master), askTimeout.duration)
      .asInstanceOf[ActorResponseMessage] match {
      case response: ActorResponseOKMessage => rootYellowPages ! DeletionRequestMessage(response.actor, "CollaborationActor", Topic() :+ Communication :+ Collaborations :+ RabbitMQ, Master)
      case _ => fail
    }
  }

  private def registerFakeCollaborationMemberActor(): Unit = {
    val fakeCollaborationMemberActor = ActorContainer.actorSystem.actorOf(Props(new Actor {
      override def receive: Receive = {
        case message: PublishErrorMessageInCollaborationExchange => expectedDBError = Some(message.message)
      }
    }))
    rootYellowPages ! RegistrationRequestMessage(fakeCollaborationMemberActor, "fakeCollaborationMemberActor", Topic() :+ Communication :+ Collaborations :+ RabbitMQ, Master)
  }
}
