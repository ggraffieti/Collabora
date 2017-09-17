package org.gammf.collabora.communication.actors

import akka.actor.{ActorRef, ActorSystem}
import akka.pattern.ask
import akka.testkit.{DefaultTimeout, ImplicitSender, TestKit}
import akka.util.Timeout
import com.rabbitmq.client.{Channel, Connection, ConnectionFactory}
import org.gammf.collabora.TestUtil
import org.gammf.collabora.communication.messages.{ClientUpdateMessage, SubscribeMessage}
import org.gammf.collabora.yellowpages.ActorContainer
import org.gammf.collabora.yellowpages.ActorService._
import org.gammf.collabora.yellowpages.messages._
import org.gammf.collabora.yellowpages.util.Topic
import org.gammf.collabora.yellowpages.TopicElement._
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}
import org.gammf.collabora.communication.toBytes

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.language.postfixOps

class RabbitMQSubscriberActorTest extends TestKit (ActorSystem("CollaboraTest")) with WordSpecLike with DefaultTimeout with Matchers with BeforeAndAfterAll with ImplicitSender {

  implicit protected[this] val askTimeout: Timeout = Timeout(5 second)

  var rootYellowPages: ActorRef = _

  override def beforeAll(): Unit = {
    ActorContainer.init()
    ActorContainer.createAll()
    rootYellowPages = ActorContainer.rootYellowPages

    Thread.sleep(200)
  }

  override def afterAll(): Unit = {
    ActorContainer.shutdown()
    TestKit.shutdownActorSystem(system)
  }

  "A Subscriber actor" should {

    "capture a message sent to the previously-set queue" in {
      val factory = new ConnectionFactory()
      val connection: Connection = factory.newConnection
      val channel: Channel = connection.createChannel
      val queue = channel.queueDeclare.getQueue

      channel.queueBind(queue, "updates", "test_routing_key")

      Await.result(rootYellowPages ? ActorRequestMessage(Topic() :+ Communication :+ RabbitMQ, Subscribing), askTimeout.duration)
        .asInstanceOf[ActorResponseMessage] match {
        case response: ActorResponseOKMessage =>
          response.actor ! SubscribeMessage(channel, queue)
          channel.basicPublish(TestUtil.TYPE_UPDATES, "test_routing_key", null, "some content")
          expectMsgPF() {
            case ClientUpdateMessage(body) => assert(body.equals("some content"))
          }
        case _ => fail
      }

      connection.abort()
    }
  }
}
