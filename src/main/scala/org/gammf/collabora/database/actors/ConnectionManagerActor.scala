package org.gammf.collabora.database.actors

import akka.actor.{ActorRef, Props, Stash}
import org.gammf.collabora.database.messages.{AskConnectionMessage, GetConnectionMessage}
import org.gammf.collabora.database._
import org.gammf.collabora.yellowpages.actors.BasicActor
import reactivemongo.api.{MongoConnection, MongoDriver}

import scala.util.{Failure, Success}
import org.gammf.collabora.yellowpages.util.Topic._
import org.gammf.collabora.yellowpages.ActorService._

/**
  * An actor that have the purpose of establishing and mantaining the connection with the Database
  */
class ConnectionManagerActor(override val yellowPages: ActorRef,
                             override val name: String,
                             override val topic: ActorTopic,
                             override val service: ActorService = ConnectionHandler) extends BasicActor with Stash {

  private[this] var connection: Option[MongoConnection] = None
  private[this] val mongoUri = CONNECTION_STRING

  override def preStart(): Unit = {
    val driver = MongoDriver()
    val parseUri = MongoConnection.parseURI(mongoUri)
    val tryCconnection = parseUri.map(driver.connection)
    tryCconnection match {
      case Success(c) => connection = Some(c)
      case Failure(e) => e.printStackTrace()
    }
    super[BasicActor].preStart()
  }

  override def receive: Receive = ({
    case _ : AskConnectionMessage => sender ! GetConnectionMessage(connection.get)
  }: Receive) orElse super[BasicActor].receive
}

object ConnectionManagerActor {
  /**
    * Factory method that returns a Props to create an already-registered connection manager actor.
    * @param yellowPages the reference to the yellow pages root actor.
    * @param topic the topic to which this actor is going to be registered.
    * @return the Props to use to create a connection manager actor.
    */

  def connectionManagerProps(yellowPages: ActorRef, topic: ActorTopic, name: String = "MongoConnectionManager") : Props =
    Props(new ConnectionManagerActor(yellowPages = yellowPages, name = name, topic = topic))
}