package org.gammf.collabora.yellowpages.actors

import akka.actor.{ActorRef, ActorSystem, Props}
import org.gammf.collabora.yellowpages.ActorService._
import org.gammf.collabora.yellowpages.TopicElement._
import org.gammf.collabora.yellowpages.messages.{HierarchyNode, HierarchyPrintMessage, HierarchyRequestMessage}
import org.gammf.collabora.yellowpages.util.Topic

/**
  * This is an actor that deals with prints on the output console.
  * @param yellowPages the reference to the yellow pages root actor.
  * @param name the name of this actor.
  * @param topic the topic to which this actor is registered.
  * @param service the service that this actor offers.
  */
class PrinterActor(override val yellowPages: ActorRef,
                   override val name: String,
                   override val topic: Topic[TopicElement],
                   override val service: ActorService = Printing) extends BasicActor {
  override def receive: Receive = ({
    case HierarchyPrintMessage(list) => handleHierarchy(list)
    case _ => println(_)
  }: Receive) orElse super.receive

  private[this] def handleHierarchy(list: List[HierarchyNode]): Unit = {
    println(); println("CURRENT HIERARCHY {")
    list.sortBy(n => n.level); printLevel(list.head.level)
    printList(list, list.head.level)
    def printList(l: List[HierarchyNode], lvl: Int): Unit = l match {
      case h :: t => val nLvl = math.max(lvl, h.level); if(nLvl > lvl) printLevel(nLvl); printNode(h); printList(t, nLvl)
      case _ => println(); println("} END CURRENT HIERARCHY ")
    }
    def printLevel(n: Int): Unit = { println(); println(" Level #" + n) }
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
  def printerProps(yellowPages: ActorRef, topic: Topic[TopicElement], name: String = "Printer"): Props =
    Props(new PrinterActor(yellowPages = yellowPages, name = name, topic = topic))
}

object HierarchyTest extends App {
  val system = ActorSystem("Collabora")
  val root = system.actorOf(YellowPagesActor.rootProps())
  val printer2 = system.actorOf(PrinterActor.printerProps(
    yellowPages = root, topic = Topic(Communication, RabbitMQ, Http, Database)))
  val topic1 = system.actorOf(YellowPagesActor.topicProps(
    yellowPages = root, name = "Communication_YP", topic = Topic(Communication)))
  val topic2 = system.actorOf(YellowPagesActor.topicProps(
    yellowPages = root, topic = Topic(Communication, RabbitMQ)))
  val printer = system.actorOf(PrinterActor.printerProps(
    yellowPages = root, name = "General_Printer", topic = Topic(General)))
  val topic3 = system.actorOf(YellowPagesActor.topicProps(
    yellowPages = root, name = "Communication/Database_YP", topic = Topic(Communication, Database)))
  Thread.sleep(1000)
  root ! HierarchyRequestMessage(0)
}
