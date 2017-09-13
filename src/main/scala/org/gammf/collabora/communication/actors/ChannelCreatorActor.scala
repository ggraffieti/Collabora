package org.gammf.collabora.communication.actors

import akka.actor._
import com.newmotion.akka.rabbitmq._
import com.rabbitmq.client.BuiltinExchangeType
import org.gammf.collabora.communication.messages.{ChannelCreatedMessage, PublishingChannelCreationMessage, SubscribingChannelCreationMessage}
import org.gammf.collabora.yellowpages.ActorService.ActorService
import org.gammf.collabora.yellowpages.actors.BasicActor
import org.gammf.collabora.yellowpages.util.Topic.ActorTopic
import org.gammf.collabora.yellowpages.util.Topic
import org.gammf.collabora.yellowpages.TopicElement._
import org.gammf.collabora.yellowpages.ActorService._

/**
  * @author Manuel Peruzzi
  * This is an actor that builds and returns to the sender a specific RabbitMQ channel created on the provided connection.
  */
class ChannelCreatorActor(override val yellowPages: ActorRef, override val name: String,
                          override val topic: ActorTopic, override val service: ActorService) extends BasicActor {

  override def receive: Receive = ({
    case message: SubscribingChannelCreationMessage =>
      createChannel(message.exchange, Some(message.queue), message.routingKey, sender, message)

    case message: PublishingChannelCreationMessage =>
      createChannel(message.exchange, None, message.routingKey, sender, message)

    case ChannelCreated(_) => println("[Channel Creator Actor] Channel created!")
  }: Receive) orElse super[BasicActor].receive

  private[this] def createChannel(exchange: String, queue: Option[String],
                                  routingKey: Option[String], messageSender: ActorRef, forwardMessage: Any): Unit = {
    def setup(channel: Channel, self: ActorRef) {
      channel.exchangeDeclare(exchange, BuiltinExchangeType.DIRECT, true)
      queue match {
        case Some(q) =>
          channel.queueDeclare(q, true, false, false, null)
          channel.queueBind(q, exchange, routingKey.getOrElse(""))
        case _ =>
      }
      messageSender ! ChannelCreatedMessage(channel)
    }
    getActorOrElse(Topic() :+ Communication :+ RabbitMQ, ConnectionHandler, forwardMessage).foreach(_ ! CreateChannel(ChannelActor.props(setup)))
  }
}
