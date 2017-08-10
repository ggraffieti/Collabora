package org.gammf.collabora.database.actors

import akka.actor.{Actor, ActorRef, Stash}
import org.gammf.collabora.communication.messages.PublishNotificationMessage
import org.gammf.collabora.database.messages._
import org.gammf.collabora.util.{NotificationMessageImpl, SimpleNote}
import play.api.libs.json.Json
import reactivemongo.api.collections.bson.BSONCollection
import reactivemongo.api.{Cursor, FailoverStrategy, MongoConnection}
import reactivemongo.bson.{BSON, BSONDocument}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Success}

class DBActor(connectionActor: ActorRef, notificationActor: ActorRef) extends Actor with Stash {

  var connection: Option[MongoConnection] = None

  override def preStart(): Unit = connectionActor ! new AskConnectionMessage()


  override def receive: Receive = {
    case m: GetConnectionMessage =>
      connection = Some(m.connection)
      unstashAll()
    case _ if connection.isEmpty => stash()
    case _: RequestAllNotesMessage =>
      getNotesCollection onComplete {
        case Success(notesCollection) => notesCollection.find(BSONDocument())
          .cursor[SimpleNote]().collect[List](100, Cursor.FailOnError[List[SimpleNote]]()) onComplete {
          case Success(list) => println(list.toString)
          case Failure(e) => e.printStackTrace() // TODO improve error strategy
        }
        case Failure(e) => e.printStackTrace() // TODO improve error strategy
      }
    case m: InsertNoteMessage =>
      getNotesCollection onComplete {
        case Success(notesCollection) =>
          val bsonNote: BSONDocument = BSON.write(m.message.note)
          notesCollection.insert(bsonNote) onComplete {
          case Success(_) =>
            val not = NotificationMessageImpl(messageType = "note_created", user = m.message.user, note = bsonNote.as[SimpleNote])
            notificationActor ! PublishNotificationMessage(m.message.user, Json.toJson(not))
          case Failure(e) => e.printStackTrace() // TODO better error strategy
        }
        case Failure(e) => e.printStackTrace() // TODO better error strategy
      }
  }

  private def getNotesCollection: Future[BSONCollection] = {
    if (connection.isDefined)
      connection.get.database("collabora", FailoverStrategy())
        .map(_.collection("note", FailoverStrategy()))
    else
      throw new Error() // TODO more specific error
  }
}
