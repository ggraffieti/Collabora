package org.gammf.collabora.database.actors.worker

import akka.actor.{ActorRef, Stash}
import org.gammf.collabora.util.{Collaboration, Module, Note, UpdateMessage, UpdateMessageTarget, UpdateMessageType}
import reactivemongo.bson.BSONDocument
import org.gammf.collabora.database._
import org.gammf.collabora.database.messages.{ChangeModuleState, GetConnectionMessage}

import scala.concurrent.Future

import scala.concurrent.ExecutionContext.Implicits.global

/**
  * A Worker that performs module state changement. The changement is based on previous module state and
  * the state of the notes
  * @param connectionActor the actor that mantains the connection with the DB.
  * @param dbActor the DBMaster actor, used for sending the edited module.
  */
class DBWorkerChangeModuleStateActor(connectionActor: ActorRef, dbActor: ActorRef) extends CollaborationsDBWorker[Option[BSONDocument]](connectionActor) with Stash {
  override def receive: Receive = {

    case m: GetConnectionMessage =>
      connection = Some(m.connection)
      unstashAll()

    case _ if connection.isEmpty => stash()

    case ChangeModuleState(collaborationId, moduleId) => handleModuleChangeState(collaborationId, moduleId)

  }

  private[this] def handleModuleChangeState(collaborationId: String, moduleId: String): Unit = {
    getModule(moduleId) map {
      case Some(module) => getModulesNotes(collaborationId, moduleId).map(list =>
        if (list.nonEmpty) {
          val noteListSize: Int = list.size
          noteListSize match {
            case _ if list.count(note => note.state.definition == State.TODO) == noteListSize &&
              module.state != State.TODO =>
              dbActor ! buildUpdateMessage(
                collaborationId = collaborationId,
                updatedModule = Module(id = module.id, description = module.description, state = State.TODO)
              )
            case _ if list.count(note => note.state.definition == State.DONE) == noteListSize &&
              module.state != State.DONE =>
              dbActor ! buildUpdateMessage(
                collaborationId = collaborationId,
                updatedModule = Module(id = module.id, description = module.description, state = State.DONE)
              )
            case _ if module.state != State.DOING =>
              dbActor ! buildUpdateMessage(
              collaborationId = collaborationId,
              updatedModule = Module(id = module.id, description = module.description, state = State.DOING)
            )
            case _ => // do nothing
          }
        }
      )
      case None => // do nothing
    }
  }

  /**
    * Gets a list of notes, contained in the given module.
    * @param collaborationId the id of the collaboration
    * @param moduleId the id of the module
    * @return a list of notes contained in the given module. If no note is contained in the given module
    *         returns an empty list
    */
  private[this] def getModulesNotes(collaborationId: String, moduleId: String): Future[List[Note]] = {
    find(
      selector = BSONDocument(COLLABORATION_ID -> collaborationId),
      okStrategy = bsonDocument => bsonDocument,
      failStrategy = { case _: Exception => None}
    ).map(bson => if (bson.isDefined && bson.get.as[Collaboration].notes.isDefined)
                    bson.get.as[Collaboration].notes.get.filter(note => note.module.isDefined && note.module.get == moduleId)
                  else List()
    )
  }

  /**
    * Get a module
    * @param moduleId the module ID
    * @return a [[Future]] representing the result. If the module exists Some(module) is returned. If the
    *         module not exists an empty Option is returned.
    */
  private[this] def getModule(moduleId: String): Future[Option[Module]] = {
    find(
      selector = BSONDocument(COLLABORATION_MODULES + "." + MODULE_ID -> moduleId),
      okStrategy = bsonDocument => bsonDocument,
      failStrategy = { case _: Exception => None}
    ).map(bson => if (bson.isDefined && bson.get.as[Collaboration].modules.isDefined)
                      bson.get.as[Collaboration].modules.get.find(module => module.id.get == moduleId)
                  else None
    )
  }

  private[this] def buildUpdateMessage(collaborationId: String, updatedModule: Module): UpdateMessage = {
    UpdateMessage(
      target = UpdateMessageTarget.MODULE,
      messageType = UpdateMessageType.UPDATING,
      user = "Collabora server",
      module = Some(updatedModule),
      collaborationId = Some(collaborationId)
    )
  }
}

private object State {
  val TODO: String = "To Do"
  val DOING: String = "Doing"
  val DONE: String = "Done"
}