package org.gammf.collabora.database.messages

import reactivemongo.api.MongoConnection

/**
  * Message sent by the connectionManagerActor to every actor that asked for a connection
  * @param connection the connection
  */
class GetConnectionMessage(val connection: MongoConnection) {

}
