package org.gammf.collabora.database.messages

import org.gammf.collabora.util.User

/**
  * A simple trait representing a message created from a db worker
  */
trait DBWorkerMessage

/**
  * A simple case class representing a message used to tell that a certain operation on the db is succeeded
  */
case class QueryOkMessage(queryGoneWell: QueryMessage) extends DBWorkerMessage

/**
  * A simple case class representing a message used to tell that a certain operation on the DB failed.
  * @param error the error.
  * @param username the username of the Collabora member that have caused the error.
  */
case class QueryFailMessage(error: Exception, username: String) extends DBWorkerMessage

/**
  * Simple class representing a message used to report login info (username and password) of a user. If no user is
  * found the loginInfo is None.
  * @param user user object if the user is registered. None otherwise.
  */
case class AuthenticationMessage(user: Option[User]) extends DBWorkerMessage