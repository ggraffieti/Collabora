package org.gammf.collabora.util

import org.gammf.collabora.util.CollaborationType.CollaborationType
import org.joda.time.DateTime
import play.api.libs.json.Json

/**
  * A simple implementation of the trait collaboration
  * @param id the id of the collaboration
  * @param name the name of the collaboration
  * @param collaborationType the type (group, project, private)
  * @param users a list of CollaborationUsers
  * @param modules a list of modules inside this collaboration
  * @param notes a list of notes inside this collaborations
  */
case class SimpleCollaboration(id: Option[String] = None, name: String, collaborationType: CollaborationType,
                               users: Option[List[CollaborationUser]] = None,
                               modules: Option[List[Module]] = None,
                               notes: Option[List[Note]] = None) extends Collaboration {

}

object CollectionImplicitTest extends App {
  val collaboration = SimpleCollaboration(Option.empty,
                                          "nome",
                                          CollaborationType.GROUP,
                                          Option(List(CollaborationUser("fone", CollaborationRight.ADMIN), CollaborationUser("peru", CollaborationRight.ADMIN))),
                                          Option.empty,
                                          Option(List(SimpleNote(Option("prova"),"questo è il contenuto",Option(new DateTime()),Option(Location(23.32,23.42)),Option.empty,NoteState("doing", Option("fone")),Option.empty),
                                                      SimpleNote(Option("prova2"),"questo è il contenuto2",Option(new DateTime()),Option(Location(233.32,233.42)),Option.empty,NoteState("done", Option("peru")),Option.empty))))
  val jsn = Json.toJson(collaboration)
  println("Json format: " + jsn)
  println("Object format: " + jsn.as[Collaboration])
}
