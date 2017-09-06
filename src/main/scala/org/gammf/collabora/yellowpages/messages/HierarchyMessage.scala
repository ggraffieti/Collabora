package org.gammf.collabora.yellowpages.messages

import org.gammf.collabora.yellowpages.util.ActorYellowPagesEntry
import org.gammf.collabora.yellowpages.actors.YellowPagesActor
import org.gammf.collabora.yellowpages.actors.PrinterActor

/**
  * Represents a message about the hierarchical structure of the actor system.
  */
trait HierarchyMessage

/**
  * Represents a request sent to an [[YellowPagesActor]] in order to build a hierarchical structure of the actor system.
  * @param level a number indicating the depth level of the current structure.
  */
case class HierarchyRequestMessage(level: Int) extends HierarchyMessage

/**
  * It's a response to a [[HierarchyRequestMessage]], in which an actor shares the basic info of all the actors that are situated in a deeper level.
  * @param actors the actors situated in a deeper level compared to the sender actor.
  */
case class HierarchyResponseMessage(actors: List[(Int, ActorYellowPagesEntry)])

/**
  * Represents a printing request, in which an actor asks a [[PrinterActor]] to print a hierarchical structure.
  * @param actors the actors that are part of the hierarchical structure.
  */
case class HierarchyPrintMessage(actors: List[HierarchyNode])

/**
  * Represents a node in a hierarchical structure.
  * @param level the depth level of the node.
  * @param reference the actor reference of the node.
  * @param name the actor name of the node.
  * @param topic the actor topic of the node.
  * @param service the actor service of the node.
  */
case class HierarchyNode(level: Int, reference: String, name: String, topic: String, service: String)
