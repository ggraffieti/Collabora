package org.gammf.collabora

import scala.language.implicitConversions

package object communication {

  /**
    * Implicit type conversion, useful for retrieving data in an array of bytes from the rabbitMQ queue and immediately convert
    * it to String.
    */
  implicit def fromBytes(x: Array[Byte]): String = new String(x, "UTF-8")

  /**
    * Implicit type conversion, useful for converting a String to an array of bytes and send them immediately to a RabbitMQ exchange.
    */
  implicit def toBytes(x: String): Array[Byte] = x.getBytes("UTF-8")

  /**
    * This is a simple enumeration containing the types of the client-server communication.
    */
  object CommunicationType extends Enumeration {
    type CommunicationType = Value
    val UPDATES, NOTIFICATIONS, COLLABORATIONS = Value
  }
}
