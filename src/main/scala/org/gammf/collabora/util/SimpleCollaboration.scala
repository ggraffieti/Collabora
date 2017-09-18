package org.gammf.collabora.util

import org.gammf.collabora.util.CollaborationType.CollaborationType

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
