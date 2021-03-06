package org.gammf.collabora.database.actors.worker

import org.gammf.collabora.database.messages.{DBWorkerMessage, QueryFailMessage}

/**
  * A default DB worker. A default DB Worker is a worker that acts in a very precise manner. It perform
  * requested actions, and always returns a message to the request sender, reporting the success or the
  * failment of the query. A default DB worker use [[org.gammf.collabora.database.messages.DBWorkerMessage]]
  * for reporting success or failment of an action. This actor have to be extended by every actor that acts in this way.
  * <br/><br/>
  * A DB worker behavior is schematized below.
  * <ul>
  *   <li> A message is received </li>
  *   <li> The worker performs the query/action </li>
  *   <ul>
  *     <li> If everything went good, return a [[org.gammf.collabora.database.messages.DBWorkerMessage]] indicating the success </li>
  *     <li> If something went wrong, return a [[org.gammf.collabora.database.messages.QueryFailMessage]] indicating the failment of the query/action </li>
  *   </ul>
  *   <li> The response message is sent back to the request sender. </li>
  * </ul>
  */
trait DefaultDBWorker extends DBWorker[DBWorkerMessage] {

  /**
    * The default fail strategy if something in queries gone bad.
    * @param username the username of the Collabora member that made the request.
    * @return A [[org.gammf.collabora.database.messages.QueryFailMessage]], containing the error and the username of the user that have
    *         requested the action.
    */
  protected def defaultDBWorkerFailStrategy(username: String): PartialFunction[Throwable, DBWorkerMessage] =
    { case e: Exception => QueryFailMessage(e, username) }

}
