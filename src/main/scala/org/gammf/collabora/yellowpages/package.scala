package org.gammf.collabora

import org.gammf.collabora.yellowpages.messages.{ActorOKMessage, RegistrationRequestMessage}
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

  /**
    * Implicit conversion from a [[RegistrationRequestMessage]] to a [[ActorYellowPagesEntry]].
    * Gets useful in the registration phase of an actor in the yellow pages.
    */
  implicit def registrationRequest2YellowPageEntry(reg: RegistrationRequestMessage): ActorYellowPagesEntry =
    ActorYellowPagesEntry(reg.actor, reg.topic, reg.service)

  /**
    * Implicit conversion from a [[ActorYellowPagesEntry]] to a [[ActorOKMessage]].
    * Gets useful to easily sends an actor contained in the yellow pages.
    */
  implicit def yellowPageEntry2ActorOK(entry: ActorYellowPagesEntry): ActorOKMessage =
    ActorOKMessage(entry.reference, entry.topic, entry.service)
}
