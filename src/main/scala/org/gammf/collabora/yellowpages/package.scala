package org.gammf.collabora

import akka.actor.ActorRef
import org.gammf.collabora.yellowpages.ActorService.ActorService
import org.gammf.collabora.yellowpages.messages._
import org.gammf.collabora.yellowpages.util.Topic.ActorTopic
import org.gammf.collabora.yellowpages.util.{ActorInformation, ActorYellowPagesEntry}

import language.reflectiveCalls

package object yellowpages {

  /**
    * An enumeration containing all the application related topics.
    */
  object TopicElement extends Enumeration {
    type TopicElement = Value

    val General,
    Authentication,
    Communication,
    RabbitMQ,
    Firebase,
    Collaborations,
    Notifications,
    Updates,
    Database,
    Note,
    Module,
    Member,
    Collaboration
    = Value
  }

  /**
    * An enumeration containing all the application related servicies that an actor can offer.
    */
  object ActorService extends Enumeration {
    type ActorService = Value

    val YellowPagesService,
    Printing,
    ConnectionHandler,
    ChannelHandler,
    ChannelCreating,
    Naming,
    Publishing,
    Subscribing,
    Authenticator,
    Master,
    DefaultWorker,
    ExistenceChecking,
    StateChanger,
    Getter,
    Bridging
    = Value
  }

  object entriesImplicitConversions {
    import language.implicitConversions

    private[this] type EntryType = {
      def reference: ActorRef
      def name: String
      def topic: ActorTopic
      def service: ActorService
    }
    /**
      * Implicit conversion from object to [[org.gammf.collabora.yellowpages.util.ActorYellowPagesEntry]].
      * The object type requirements are expressed by interface structure, accepting every object that provides a definition for actor, topic and service.
      */
    implicit def message2yellowPagesEntry(msg: EntryType): ActorYellowPagesEntry =
      ActorYellowPagesEntry(reference = msg.reference, name = msg.name, topic = msg.topic, service = msg.service)

    /**
      * Implicit conversion from a [[org.gammf.collabora.yellowpages.util.ActorYellowPagesEntry]] to a [[org.gammf.collabora.yellowpages.messages.ActorResponseOKMessage]].
      * Gets useful to easily sends an actor contained in the yellow pages.
      */
    implicit def yellowPagesEntry2ActorOK(entry: ActorYellowPagesEntry): ActorResponseOKMessage =
      ActorResponseOKMessage(actor = entry.reference, topic = entry.topic, service = entry.service)

    /**
      * Implicit conversion from a [[org.gammf.collabora.yellowpages.util.ActorYellowPagesEntry]] to [[org.gammf.collabora.yellowpages.messages.RedirectionRequestMessage]].
      * Gets useful to easily send a redirection message, in order to move an entry to a different [[org.gammf.collabora.yellowpages.actors.YellowPagesActor]].
      */
    implicit def yellowPagesEntry2RedirectionRequest(entry: ActorYellowPagesEntry): RedirectionRequestMessage =
      RedirectionRequestMessage(reference = entry.reference, name = entry.name, topic = entry.topic, service = entry.service)

    /**
      * Implicit conversion from a [[org.gammf.collabora.yellowpages.util.ActorYellowPagesEntry]] to [[org.gammf.collabora.yellowpages.messages.DeletionRequestMessage]].
      */
    implicit def yellowPagesEntry2DeleteRequest(entry: ActorYellowPagesEntry): DeletionRequestMessage =
      DeletionRequestMessage(reference = entry.reference, name = entry.name, topic = entry.topic, service = entry.service)

    /**
      * Implicit conversion from a list of tuples compound by [[org.gammf.collabora.yellowpages.util.ActorYellowPagesEntry]]
      * with a depth level to a list of [[org.gammf.collabora.yellowpages.messages.HierarchyNode]].
      */
    implicit def entryList2hierarchyNodeList(list: List[(Int, ActorYellowPagesEntry)]): List[HierarchyNode] =
      list.map(yp => HierarchyNode(level = yp._1, reference = yp._2.reference.toString, name = yp._2.name.toString, topic = yp._2.topic.toString, service = yp._2.service.toString))

    /**
      * Implicit conversione from a [[org.gammf.collabora.yellowpages.messages.ActorResponseOKMessage]] to [[org.gammf.collabora.yellowpages.util.ActorInformation]].
      */
    implicit def actorOkMessage2ActorInformation(message: ActorResponseOKMessage): ActorInformation =
      ActorInformation(message.actor, message.topic, message.service)
  }
}
