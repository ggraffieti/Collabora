package org.gammf.collabora

import org.gammf.collabora.yellowpages.messages._
import org.gammf.collabora.yellowpages.util.ActorYellowPagesEntry

package object yellowpages {

  /**
    * An enumeration containing all the application related topics.
    */
  object TopicElement extends Enumeration {
    type TopicElement = Value

    // Communication related topics
    val Communication,
    Rabbitmq,
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

    /**
      * Implicit conversion from a [[InsertionRequestMessage]] to a [[ActorYellowPagesEntry]].
      * Gets useful in the registration phase of an actor in the yellow pages.
      */
    implicit def insertionRequest2YellowPagesEntry(reg: InsertionRequestMessage): ActorYellowPagesEntry =
      ActorYellowPagesEntry(reference = reg.actor, topic = reg.topic, service = reg.service)

    /**
      * Implicit conversion from a [[ActorYellowPagesEntry]] to a [[ActorOKMessage]].
      * Gets useful to easily sends an actor contained in the yellow pages.
      */
    implicit def yellowPagesEntry2ActorOK(entry: ActorYellowPagesEntry): ActorOKMessage =
      ActorOKMessage(actor = entry.reference, topic = entry.topic, service = entry.service)

    /**
      * Implicit conversion from a [[ActorRequestMessage]] to a [[ActorErrorMessage]].
      * Gets useful to easily send a message error related to an actor request that, for some reason, can not be satisfied.
      */
    implicit def actorRequest2ActorError(act: ActorRequestMessage): ActorErrorMessage =
      ActorErrorMessage(topic = act.topic, service = act.service)

    /**
      * Implicit conversion from a [[ActorYellowPagesEntry]] to [[RedirectionRequestMessage]].
      * Gets useful to easily send a redirection meesage, in order to move an entry to a different [[org.gammf.collabora.yellowpages.actors.YellowPagesActor]].
      */
    implicit def yellowPagesEntry2RedirectionRequest(entry: ActorYellowPagesEntry): RedirectionRequestMessage =
      RedirectionRequestMessage(actor = entry.reference, topic = entry.topic, service = entry.service)

    /**
      * Implicit conversion from a [[RedirectionRequestMessage]] to [[ActorRedirectionOKMessage]].
      * Gets useful to easily send a positive response to an [[RedirectionRequestMessage]].
      */
    implicit def redirection2RedirectionOK(red: RedirectionRequestMessage): ActorRedirectionOKMessage =
      ActorRedirectionOKMessage(actor = red.actor, topic = red.topic, service = red.service)

    implicit def RedirectionOk2yellowPagesEntry(red: ActorRedirectionOKMessage): ActorYellowPagesEntry =
      ActorYellowPagesEntry(reference = red.actor, topic = red.topic, service = red.service)
  }
}
