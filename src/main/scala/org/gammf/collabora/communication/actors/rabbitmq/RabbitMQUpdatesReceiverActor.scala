package org.gammf.collabora.communication.actors.rabbitmq

import akka.actor._
import org.gammf.collabora.communication.CommunicationType
import org.gammf.collabora.communication.messages._
import org.gammf.collabora.util.UpdateMessage
import org.gammf.collabora.yellowpages.ActorService._
import org.gammf.collabora.yellowpages.TopicElement._
import org.gammf.collabora.yellowpages.actors.BasicActor
import org.gammf.collabora.yellowpages.messages._
import org.gammf.collabora.yellowpages.util.Topic
import org.gammf.collabora.yellowpages.util.Topic._
import play.api.libs.json.{JsError, JsSuccess, Json}

/**
  * This actor completely manages the whole message receiving from the rabbitMQ exchange Updates.
  * Needs a subscribing channel in order to receive the messages from the exchange, when it acquires a valid channel it
  * starts listening, serching for client [[org.gammf.collabora.util.UpdateMessage]] to forward to an actor that can access the database.
  */

/**
  * This is an actor that manages the reception of client updates.
  */
class RabbitMQUpdatesReceiverActor(override val yellowPages: ActorRef, override val name: String,
                                   override val topic: ActorTopic, override val service: ActorService = Master) extends BasicActor {

  private[this] var subQueue: Option[String] = None

  override def receive: Receive = ({
    case message: RegistrationResponseMessage =>
      getActorOrElse(Topic() :+ Communication :+ RabbitMQ, Naming, message).foreach(actorRef => actorRef ! ChannelNamesRequestMessage(CommunicationType.UPDATES))
    case message: ChannelNamesResponseMessage =>
      subQueue = message.queue
      getActorOrElse(Topic() :+ Communication :+ RabbitMQ, ChannelCreating, message).foreach(_ ! SubscribingChannelCreationMessage(message.exchange, subQueue.get, None))
    case message: ChannelCreatedMessage =>
      getActorOrElse(Topic() :+ Communication :+ RabbitMQ, Subscribing, message).foreach(_ ! SubscribeMessage(message.channel, subQueue.get))
    case message: ClientUpdateMessage =>
      Json.parse(message.text).validate[UpdateMessage] match {
        case updateMessage: JsSuccess[UpdateMessage] => getActorOrElse(Topic() :+ Database, Master, message).foreach(_ ! updateMessage.value)
        case error: JsError => println("[" + name + "] Error: " + error)
      }
  }: Receive) orElse super[BasicActor].receive
}

object RabbitMQUpdatesReceiverActor {
  /**
    * Factory method that returns a Props to create an already-registered updates receiver actor.
    * @param yellowPages the reference to the yellow pages root actor.
    * @param topic the topic to which this actor is going to be registered.
    * @return the Props to use to create a updates receiver actor.
    */

  def updatesReceiverProps(yellowPages: ActorRef, topic: ActorTopic, name: String = "UpdatesReceiver") : Props =
    Props(new RabbitMQUpdatesReceiverActor(yellowPages = yellowPages, name = name, topic = topic))
}
