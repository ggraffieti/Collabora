package org.gammf.collabora

import akka.actor.{Actor, ActorSystem, Props}
import org.gammf.collabora.database.DBActor
import org.gammf.collabora.database.messages.{InsertNoteMessage, RequestAllNotesMessage}
import org.gammf.collabora.util.Note
import reactivemongo.api.collections.bson.BSONCollection
import reactivemongo.api.{Cursor, FailoverStrategy, MongoConnection, MongoDriver}
import reactivemongo.bson.{BSONArray, BSONDocument}
import reactivemongo.play.json._
import play.api.libs.json._

import scala.concurrent.{Await, Future}
import scala.concurrent.duration._
import scala.util.Success
import scala.util.Failure

class Test extends Actor {

  val mongoUri = "mongodb://localhost:27017/collabora?authMode=scram-sha1"

  import scala.concurrent.ExecutionContext.Implicits.global

  val driver = MongoDriver()
  val parseUri = MongoConnection.parseURI(mongoUri)
  val connection = parseUri.map(driver.connection(_))

  def collFut: Future[BSONCollection] = connection.get.database("collabora", FailoverStrategy())
    .map(_.collection("collaboration", FailoverStrategy()))
  val users: BSONCollection = Await.result(collFut, 10 seconds)

  def getUser(username: String): Future[List[BSONDocument]]= {
    // { "age": { "$gt": 27 } }
    val query = BSONDocument("users" -> BSONDocument("$elemMatch" -> BSONDocument("$eq" -> username)))

    // MongoDB .findOne
    users.find(query).cursor[BSONDocument]().collect[List](10, Cursor.FailOnError[List[BSONDocument]]())
  }

  override def receive: Receive = {
    case "asd" => println("HAHA")
    case name: String => {
      val us = getUser(name)
      us onComplete {
        case Success(l) => l.foreach(e => println(BSONFormats.BSONDocumentFormat.writes(e).as[JsObject]))
        case Failure(e) => println("Error " + e)
      }
    }
    case _ => println("NO")

  }
}

object Main extends App {
  /*val system = ActorSystem("HelloSystem")
  // default Actor constructor
  val helloActor = system.actorOf(Props[Test], name = "helloactor")
  helloActor ! "manuelperuzzi"*/


  val mongoUri = "mongodb://localhost:27017/collabora?authMode=scram-sha1"

  val driver = MongoDriver()
  val parseUri = MongoConnection.parseURI(mongoUri)
  val connection = parseUri.map(driver.connection(_))

  val system = ActorSystem("HelloSystem")
  // default Actor constructor
  val dbActor = system.actorOf(Props.create(classOf[DBActor], connection.get))

  val note: Note = new Note(content = "Insertion test", state = "toDo", location = Some(13.2541, 45.2541))

  dbActor ! new InsertNoteMessage(note)
  dbActor ! new RequestAllNotesMessage()
  dbActor ! new RequestAllNotesMessage()
  dbActor ! new RequestAllNotesMessage()
  dbActor ! new RequestAllNotesMessage()
  dbActor ! new RequestAllNotesMessage()
  dbActor ! new RequestAllNotesMessage()

}
