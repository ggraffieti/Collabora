package org.gammf.collabora.yellowpages.util

import akka.actor.{ActorSystem, Props}
import org.gammf.collabora.communication.actors.RabbitMQNamingActor
import org.gammf.collabora.yellowpages.TopicElement._
import org.gammf.collabora.yellowpages.ActorService._


trait CachableSet[A] {
  def ::(element: A): Unit
  def get(predicate: A => Boolean) : Option[(A, Boolean)]
}

trait CachableSetImpl[A] extends CachableSet[A] {
  private[this] val shortTimeout = 5 * 60 * 1000
  private[this] val longTimeout = 3 * 60 * 60 * 1000
  private[this] var cachableSet: Set[(A, Long)] = Set()

  override def ::(element: A): Unit = cachableSet = cachableSet + ((element, System.currentTimeMillis()))

  override def get(predicate: (A) => Boolean): Option[(A, Boolean)] = {
    cachableSet.find(tuple => predicate(tuple._1)) match {
      case Some((element, timestamp)) if isTimestampValid(timestamp) => Some((element, true))
      case Some((element, timestamp)) if !isTimestampValid(timestamp) && !isOverLongTimeout(timestamp) => Some((element, false))
      case Some((element, timestamp)) if isOverLongTimeout(timestamp) => cachableSet = cachableSet - ((element,timestamp)); None
      case _ => None
    }
  }

  private[this] def isTimestampValid(timestamp: Long): Boolean = System.currentTimeMillis - timestamp < shortTimeout

  private[this] def isOverLongTimeout(timestamp: Long): Boolean = System.currentTimeMillis() - timestamp > longTimeout
}

case class BasicCachableSet[A]() extends CachableSetImpl[A]

object CachableSet {
  def apply[A](): CachableSet[A] = BasicCachableSet[A]()
}

object Test extends App {
  val system = ActorSystem("CollaboraServer")
  val naming = system.actorOf(Props[RabbitMQNamingActor], "naming")

  val set = CachableSet[ActorInformation]()
  ActorInformation(naming, Topic() :+ General, Naming) :: set
  val entry = set get (yp => yp.service == Naming && yp.topic == Topic() :+ General)
  val entry2 = set get (yp => yp.service == Naming && yp.topic == Topic() :+ RabbitMQ)
  println(entry)
  println(entry2.isEmpty)
  Thread.sleep(10000)
  val entry3 = set get (yp => yp.service == Naming && yp.topic == Topic() :+ General)
  println(entry3.isEmpty)
}
