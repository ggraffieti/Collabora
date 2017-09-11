package org.gammf.collabora.yellowpages.actors

import akka.actor.{ActorRef, ActorSystem, Props}
import org.gammf.collabora.yellowpages.ActorService._
import org.gammf.collabora.yellowpages.messages.{HierarchyNode, HierarchyPrintMessage, HierarchyRequestMessage}
import org.gammf.collabora.yellowpages.util.Topic
import org.gammf.collabora.yellowpages.TopicElement._
import org.gammf.collabora.yellowpages.util.Topic.ActorTopic

import scala.annotation.tailrec

/**
  * This is an actor that deals with prints on the output console.
  * @param yellowPages the reference to the yellow pages root actor.
  * @param name the name of this actor.
  * @param topic the topic to which this actor is registered.
  * @param service the service that this actor offers.
  */
class PrinterActor(override val yellowPages: ActorRef,
                   override val name: String,
                   override val topic: ActorTopic,
                   override val service: ActorService = Printing) extends BasicActor {
  override def receive: Receive = ({
    case HierarchyPrintMessage(list) => handleHierarchy(list.sortWith(_.level < _.level))
    case msg: String => println(msg)
  }: Receive) orElse super.receive

  private[this] def handleHierarchy(list: List[HierarchyNode]): Unit = {
    println(); println("[" + name + "] CURRENT HIERARCHY {")
    printLevel(list.head.level); printList(list, list.head.level)
    def printLevel(n: Int): Unit = { println(); println(" Level #" + n) }
    @tailrec def printList(l: List[HierarchyNode], lvl: Int): Unit = l match {
      case h :: t => val nLvl = math.max(lvl, h.level); if(nLvl > lvl) printLevel(nLvl); printNode(h); printList(t, nLvl)
      case _ => println(); println("} END CURRENT HIERARCHY")
    }
    def printNode(n: HierarchyNode): Unit =
      println("   " + n.name + " => INFO[Topic: " + n.topic + ", Service: " + n.service + ", Reference: " + n.reference + "]")
  }
}

object PrinterActor {

  /**
    * Factory methods that returns a [[Props]] to create a printer actor registered to the specified topic.
    * @param yellowPages the reference to the yellow pages root actor.
    * @param topic the topic to which this actor is going to be registered.
    * @return the [[Props]] to use to create a printer actor.
    */
  def printerProps(yellowPages: ActorRef, topic: ActorTopic, name: String = "Printer"): Props =
    Props(new PrinterActor(yellowPages = yellowPages, name = name, topic = topic))
}

object HierarchyTest extends App {
  val system = ActorSystem("Collabora")
  val root = system.actorOf(YellowPagesActor.rootProps())
  val printer2 = system.actorOf(PrinterActor.printerProps(yellowPages = root, topic = Topic() :+ Communication :+ RabbitMQ :+ Http :+ Database))
  val topic1 = system.actorOf(YellowPagesActor.topicProps(yellowPages = root, name = "Communication_YP", topic = Topic() :+ Communication))
  val topic2 = system.actorOf(YellowPagesActor.topicProps(yellowPages = root, topic = Topic() :+ Communication :+ RabbitMQ, name = "Communication/RabbitMQ_YP"))
  val printer = system.actorOf(PrinterActor.printerProps(yellowPages = root, name = "General_Printer", topic = Topic() :+ General))
  val printer3 = system.actorOf(PrinterActor.printerProps(yellowPages = root, name = "General_Printer2", topic = Topic() :+ General))
  val topic3 = system.actorOf(YellowPagesActor.topicProps(yellowPages = root, name = "Communication/Http_YP", topic = Topic() :+ Communication :+ Http))
  val topic4 = system.actorOf(YellowPagesActor.topicProps(root, Topic() :+ Communication, name = "Communication_YP2"))
  Thread.sleep(1000)
  root ! HierarchyRequestMessage(0)
}
