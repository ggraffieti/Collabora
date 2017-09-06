package org.gammf.collabora

import akka.actor.ActorRef
import org.gammf.collabora.yellowpages.ActorService.ActorService
import org.gammf.collabora.yellowpages.TopicElement.TopicElement
import org.gammf.collabora.yellowpages.messages._
import org.gammf.collabora.yellowpages.util.{ActorYellowPagesEntry, Topic}

import language.reflectiveCalls

package object yellowpages {

  /**
    * An enumeration containing all the application related topics.
    */
  object TopicElement extends Enumeration {
    type TopicElement = Value

    val General,
    // Communication related topics
    Communication,
    RabbitMQ,
    Firebase,
    Http,
    // Database related topics
    Database
    //TODO list all the database related topics
    = Value
  }

  /**
    * An enumeration containing all the application related servicies that an actor can offer.
    */
  object ActorService extends Enumeration {
    type ActorService = Value

    // Yellow pages related services
    val YellowPagesService,
    Printing,
    // Communication related services
    ChannelCreating,
    Naming,
    Publishing,
    Subscribing,
    NotificationSending,
    CollaborationSending,
    UpdatesReceiving
    // TODO update the list with http related services
    // Database related services
    // TODO list all the database related services
    = Value
  }

  object entriesImplicitConversions {
    import language.implicitConversions

    private[this] type EntryType = {
      def reference: ActorRef
      def name: String
      def topic: Topic[TopicElement]
      def service: ActorService
    }
    /**
      * Implicit conversion from object to [[ActorYellowPagesEntry]].
      * The object type requirements are expressed by interface structure, accepting every object that provides a definition for actor, topic and service.
      */
    implicit def message2yellowPagesEntry(msg: EntryType): ActorYellowPagesEntry =
      ActorYellowPagesEntry(reference = msg.reference, name = msg.name, topic = msg.topic, service = msg.service)

    /**
      * Implicit conversion from a [[ActorYellowPagesEntry]] to a [[ActorResponseOKMessage]].
      * Gets useful to easily sends an actor contained in the yellow pages.
      */
    implicit def yellowPagesEntry2ActorOK(entry: ActorYellowPagesEntry): ActorResponseOKMessage =
      ActorResponseOKMessage(actor = entry.reference, topic = entry.topic, service = entry.service)

    /**
      * Implicit conversion from a [[ActorYellowPagesEntry]] to [[RedirectionRequestMessage]].
      * Gets useful to easily send a redirection message, in order to move an entry to a different [[org.gammf.collabora.yellowpages.actors.YellowPagesActor]].
      */
    implicit def yellowPagesEntry2RedirectionRequest(entry: ActorYellowPagesEntry): RedirectionRequestMessage =
      RedirectionRequestMessage(reference = entry.reference, name = entry.name, topic = entry.topic, service = entry.service)

    /**
      * Implicit conversion from a [[List]] of tuples compound by [[ActorYellowPagesEntry]] with a depth level to a [[List]] of [[HierarchyNode]].
      */
    implicit def entryList2hierarchyNodeList(list: List[(Int, ActorYellowPagesEntry)]): List[HierarchyNode] =
      list.map(yp => HierarchyNode(level = yp._1, reference = yp._2.reference.toString(), topic = yp._2.topic.toString, service = yp._2.service.toString))
  }
}
