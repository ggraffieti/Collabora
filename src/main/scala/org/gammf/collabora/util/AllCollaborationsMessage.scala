package org.gammf.collabora.util

import play.api.libs.functional.syntax._
import play.api.libs.json.{JsPath, Reads, Writes}


trait AllCollaborationsMessage {

  def username: String
  def collaborationList: List[Collaboration]

}

object AllCollaborationsMessage {

  def apply(username: String, collaborationList: List[Collaboration]): AllCollaborationsMessage = SimpleAllCollaborationsMessage(username, collaborationList)

  def unapply(arg: AllCollaborationsMessage): Option[(String, List[Collaboration])] = Some((arg.username, arg.collaborationList))


  implicit val allCollaborationMessageReads: Reads[AllCollaborationsMessage] = (
      (JsPath \ "username").read[String] and
        (JsPath \ "collaborationList").read[List[Collaboration]]
      )(AllCollaborationsMessage.apply _)

  implicit val allCollaborationMessageWrites: Writes[AllCollaborationsMessage] = (
    (JsPath \ "username").write[String] and
      (JsPath \ "collaborationList").write[List[Collaboration]]
    )(unlift(AllCollaborationsMessage.unapply))

}
