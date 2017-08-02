package org.gammf.collabora


import akka.actor.{Actor, ActorSystem, Props}
import reactivemongo.api.collections.bson.BSONCollection
import reactivemongo.api.{ MongoConnection, MongoDriver}
import reactivemongo.bson.BSONDocument

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

  val futureConnection = Future.fromTry(connection)
  def usersFut: Future[BSONCollection] = futureConnection.flatMap(_.database("collabora")).map(_.collection("user"))
  val users: BSONCollection = Await.result(usersFut, 10 seconds)

  def getUser(username: String): Future[Option[BSONDocument]] = {
    // { "age": { "$gt": 27 } }
    val query = BSONDocument("username" -> username)

    // MongoDB .findOne
    users.find(query).one[BSONDocument]
  }

  override def receive: Receive = {
    case "asd" => println("HAHA")
    case name: String => {
      val us = getUser(name)
      us onComplete {
        case Success(op) => {
          if (op.isDefined) {
            println(op.get.getAs[String]("email").get)
          } else println("no user defined")
        }
        case Failure(e) => println("Error " + e)
      }
    }
    case _ => println("NO")

  }
}

object Main extends App {
  val system = ActorSystem("HelloSystem")
  // default Actor constructor
  val helloActor = system.actorOf(Props[Test], name = "helloactor")
  helloActor ! "manuelperuzzi"
  helloActor ! "asd"
  helloActor ! "federicovitali"
  helloActor ! "johndoe"
  helloActor ! 54
}
