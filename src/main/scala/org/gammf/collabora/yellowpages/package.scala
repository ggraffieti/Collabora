package org.gammf.collabora

package object yellowpages {
  /**
    * An enumeration containing all the application related topics.
    */
  object TopicElement extends Enumeration {
    val communication,
    rabbitmq,
    firebase,
    http = Value

    val database = Value
    //TODO list all the database related topics
  }

  /**
    * An enumeration containing all the application related servicies that an actor can offer.
    */
  object ActorService extends Enumeration {
    // Yellow pages related services
    val yellowPagesService = Value

    // Communication related services
    // TODO update the list with http related services
    val channelCreating,
    naming,
    publishing,
    subscribing,
    notificationSending,
    collaborationSending,
    updatesReceiving = Value

    // Database related services
    // TODO list all the database related services
  }
}
