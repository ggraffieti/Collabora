package org.gammf.collabora.yellowpages.actors

import akka.actor.{ActorRef, Props}
import org.gammf.collabora.yellowpages.ActorService._
import org.gammf.collabora.yellowpages.messages.{HierarchyNode, HierarchyPrintMessage}
import org.gammf.collabora.yellowpages.util.Topic.ActorTopic

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
    case HierarchyPrintMessage(list) => handleHierarchy(list)
    case msg: String => println(msg)
  }: Receive) orElse super.receive

  private[this] def handleHierarchy(nodes: List[HierarchyNode]): Unit = {
    println(); println("[" + name + "] " + nodes.size + " actors found, CURRENT HIERARCHY {"); println()
    nodes.foreach(printNode); println(); println("} END CURRENT HIERARCHY")
    def printNode(n: HierarchyNode): Unit = {
      println(getIndent(n.level) + n.name + " => INFO[" + n.topic + ", " + n.service + ", " + n.reference + "]")
      def getIndent(level: Int): String = "    " * (level - nodes.map(_.level).min)
    }
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
