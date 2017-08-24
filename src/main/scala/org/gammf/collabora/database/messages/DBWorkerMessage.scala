package org.gammf.collabora.database.messages

/**
  * A simple trait representing a message created from a db worker
  */
trait DBWorkerMessage

/**
  * A simple case class representing a message used to tell that a certain operation on the db is succeeded
  */
case class QueryOkMessage(queryGoneWell: QueryMessage) extends DBWorkerMessage