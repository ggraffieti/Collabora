package org.gammf.collabora.database.messages

import reactivemongo.api.MongoConnection

case class AskConnectionMessage()
case class GetConnectionMessage(connection: MongoConnection);
