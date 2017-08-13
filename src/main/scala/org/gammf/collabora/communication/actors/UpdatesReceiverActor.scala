package org.gammf.collabora.communication.actors

import akka.actor._
import org.gammf.collabora.communication.Utils.CommunicationType
import org.gammf.collabora.communication.messages._
import org.gammf.collabora.database.messages.InsertNoteMessage
import org.gammf.collabora.util.UpdateMessageImpl
import play.api.libs.json.{JsError, JsSuccess, Json}

/**
  * @author Manuel Peruzzi
  */

/**
  * This is an actor that manages the reception of client updates.
  * @param connection the open connection with the rabbitMQ broker.
  * @param naming the reference to a rabbitMQ naming actor.
  * @param channelCreator the reference to a channel creator actor.
  * @param subscriber the reference to a subscriber actor.
  */
class UpdatesReceiverActor(connection: ActorRef, naming: ActorRef, channelCreator: ActorRef,
                           subscriber: ActorRef, dbActor: ActorRef) extends Actor {

  private[this] var subQueue: Option[String] = None

  override def receive: Receive = {
    case StartMessage => naming ! ChannelNamesRequestMessage(CommunicationType.UPDATES)
    case ChannelNamesResponseMessage(exchange, queue) =>
      subQueue = queue
      channelCreator ! SubscribingChannelCreationMessage(connection, exchange, subQueue.get, None)
    case ChannelCreatedMessage(channel) => subscriber ! SubscribeMessage(channel, subQueue.get)
    case ClientUpdateMessage(text) =>
/*      Json.parse(text).validate[UpdateMessageImpl] match {
        case m: JsSuccess[UpdateMessageImpl] => //dbActor ! new InsertNoteMessage(m.value)
        case error: JsError => println(error)
      }  */
    case _ => println("[Updates Receiver Actor] Huh?")
  }
}
