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
  
  yellowPages ! RegistrationRequestMessage(self, topic, service)

  override def receive: Receive = {
    case RegistrationOKMessage() => println("[" + name + "] Registration OK.")
    case RegistrationErrorMessage() => println("[" + name + "] Registration Error.")
      yellowPages ! RegistrationRequestMessage(self, topic, service)
    case _ => println("["+ name + "] Huh?")
  }
}
