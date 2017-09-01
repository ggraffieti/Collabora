package org.gammf.collabora.communication.actors

import akka.actor.{Actor, ActorRef, ActorSystem, Props, Stash}
import com.newmotion.akka.rabbitmq.{Channel, ConnectionActor, ConnectionFactory}
import org.gammf.collabora.communication.Utils.CommunicationType
import org.gammf.collabora.communication.messages._
import play.api.libs.json.{JsValue, Json}

/**
  * @author Manuel Peruzzi
  */

/**
  * This is an actor that sends all the information needed by a user that has just been added to a collaboration.
  * @param connection the open connection with the rabbitMQ broker.
  * @param naming the reference to a rabbitMQ naming actor.
  * @param channelCreator the reference to a channel creator actor.
  * @param publisher the reference to a publisher actor.
  */
class CollaborationMembersActor(connection: ActorRef, naming: ActorRef, channelCreator: ActorRef,
                                publisher: ActorRef) extends Actor with Stash {

  private[this] var pubChannel: Option[Channel] = None
  private[this] var pubExchange: Option[String] = None

  override def receive: Receive = {
    case StartMessage => naming ! ChannelNamesRequestMessage(CommunicationType.COLLABORATIONS)
    case ChannelNamesResponseMessage(exchange, _) =>
      pubExchange = Some(exchange)
      channelCreator ! PublishingChannelCreationMessage(connection, exchange, None)
    case ChannelCreatedMessage(channel) =>
      pubChannel = Some(channel)
      unstashAll()
    case PublishMemberAddedMessage(username, message) =>
      pubChannel match {
        case Some(channel) =>
          publisher ! PublishMessage(channel, pubExchange.get, Some(username), Json.toJson(message))
        case _ => stash()
      }

    case PublishUserLoginMessage(username, message) =>
      pubChannel match {
        case Some(channel) => publisher ! PublishMessage(channel, pubExchange.get, Some(username), Json.toJson(message))
        case _ => stash
      }

    case _ => println("[CollaborationMembersActor] Huh?")
  }
}

/**
  * This is a simple application that uses the Collaboration Members Actor.
  */
object UseCollaborationMembersActor extends App {
  implicit val system = ActorSystem()
  val factory = new ConnectionFactory()
  val connection = system.actorOf(ConnectionActor.props(factory), "rabbitmq")

  val naming = system.actorOf(Props[RabbitMQNamingActor], "naming")
  val channelCreator = system.actorOf(Props[ChannelCreatorActor], "channelCreator")
  val publisher = system.actorOf(Props[PublisherActor], "publisher")
  val collaborationMember = system.actorOf(Props(
    new CollaborationMembersActor(connection, naming, channelCreator, publisher)), "collaboration-members")

  val message : JsValue = Json.parse("""
  {
      "user": "manuelperuzzi",
      "collaboration": {
        "id": "arandomidofarandomcollaboration",
        "name": "random-collaboration",
        "collaborationType": "group",
        "users": [
          {
            "username": "manuelperuzzi",
            "email": "manuel.peruzzi@studio.unibo.it",
            "name": "Manuel",
            "surname": "Peruzzi",
            "right": "admin"
          }
        ],
        "notes": [
          {
            "id": "arandomidofarandomnote",
            "content": "some content",
            "state": {
              "definition": "doing",
              "username": "manuelperuzzi"
            }
          }
        ]
      }
  }""")

  //collaborationMember ! PublishMemberAddedMessage("maffone", message)

  //Thread.sleep(1000)
  //collaborationMember ! StartMessage
}
