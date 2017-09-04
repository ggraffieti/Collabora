package org.gammf.collabora.yellowpages.actors

import akka.actor.{Actor, ActorRef}
import org.gammf.collabora.yellowpages.ActorService.ActorService
import org.gammf.collabora.yellowpages.TopicElement.TopicElement
import org.gammf.collabora.yellowpages.messages._
import org.gammf.collabora.yellowpages.util.Topic

trait BasicActor extends Actor {
  def yellowPages: ActorRef
  def name: String
  def topic: Topic[TopicElement]
  def service: ActorService

  override def preStart(): Unit = super.preStart; yellowPages ! RegistrationRequestMessage(self, topic, service)

  override def receive: Receive = {
    case RegistrationOKMessage() => println("[" + name + "] Registration OK.")
      // TODO remove this message from here and handle it in all the subclasses.
    case RegistrationErrorMessage() => println("[" + name + "] Registration Error.")
      // TODO add a waiting time. Example: wait 3 seconds before sending another registration request.
      yellowPages ! RegistrationRequestMessage(self, topic, service)
    case _ => println("["+ name + "] Huh?")
  }
}
