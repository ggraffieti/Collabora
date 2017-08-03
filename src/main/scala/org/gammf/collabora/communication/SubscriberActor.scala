package org.gammf.collabora.communication

import akka.actor._
import com.newmotion.akka.rabbitmq._
import com.rabbitmq.client.BuiltinExchangeType
import Utils._
/**
  * Created by mperuzzi on 03/08/17.
  */
class SubscriberActor extends Actor {

  override def receive: Receive = {
    case SubscribeMessage(channel, queue) =>
      val consumer = new DefaultConsumer(channel) {
        override def handleDelivery(consumerTag: String, envelope: Envelope,
                                    properties: BasicProperties, body: Array[Byte]) {
          println("Received: " + fromBytes(body) + " with routing key " + envelope.getRoutingKey)
          channel.basicAck(envelope.getDeliveryTag, false)
        }
      }
      channel.basicConsume(queue, false, consumer)
      println("Subscribtion started!")
    case _ => println("Huh?")
  }

}

object UseSubscriberActor extends App {
  implicit val system = ActorSystem()
  val factory = new ConnectionFactory()
  val connection = system.actorOf(ConnectionActor.props(factory), "rabbitmq")
  val exchange = "notifications"
  val queue = "notify.username"
  val routingKey = "notify.collaborationID"
  val subscribeName = exchange + "-" + queue + "-" + "subscribe"

  def setupSubscriber(channel: Channel, self: ActorRef) {
    channel.exchangeDeclare(exchange, BuiltinExchangeType.DIRECT, true)
    channel.queueDeclare(queue, true, false, false, null)
    channel.queueBind(queue, exchange, routingKey)
    val subscriber = system.actorOf(Props[SubscriberActor], subscribeName)
    subscriber ! SubscribeMessage(channel, queue)
  }
  connection ! CreateChannel(ChannelActor.props(setupSubscriber), Some(subscribeName))
}