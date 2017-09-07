package org.gammf.collabora.database.messages

import reactivemongo.api.MongoConnection

/**
  * A message sent to the [[org.gammf.collabora.database.actors.ConnectionManagerActor]] in order to obtain the connection with the database.
  */
case class AskConnectionMessage()

/**
  * The response of a [[AskConnectionMessage]], sent by the [[org.gammf.collabora.database.actors.ConnectionManagerActor]] in order to get the
  * connection to the asker actor.
  * @param connection the database connection.
  */
case class GetConnectionMessage(connection: MongoConnection);
