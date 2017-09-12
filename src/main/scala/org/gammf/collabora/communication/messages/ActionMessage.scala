package org.gammf.collabora.communication.messages

import com.newmotion.akka.rabbitmq.Channel
import play.api.libs.json.JsValue

/**
  * Simple trait that represents a message about the publish/subscribe communication.
  */
sealed trait ActionMessage extends Message

/**
  * Contains the information needed to make a subscription on the provided queue.
  * @param channel the rabbitMQ channel.
  * @param queue the subscription queue.
  */
case class SubscribeMessage(channel: Channel, queue: String) extends ActionMessage

/**
  * Contains the information needed by a publisher to publish a message on the rabbitMQ broker.
  * @param channel the rabbitMQ channel.
  * @param exchange the rabbitMQ exchange to use for the pubblication.
  * @param routingKey the routing key to be used to identify the concerned queues.
  * @param message the message to be published.
  */
case class PublishMessage(channel: Channel, exchange: String, routingKey: Option[String],
                          message: JsValue) extends ActionMessage