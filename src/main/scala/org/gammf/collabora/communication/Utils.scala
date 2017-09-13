package org.gammf.collabora.communication

/**
  * This is a simple utility class.
  */
object Utils {

  implicit def fromBytes(x: Array[Byte]): String = new String(x, "UTF-8")
  implicit def toBytes(x: Long): Array[Byte] = x.toString.getBytes("UTF-8")

  /**
    * This is a simple enumeration containing the types of the client-server communication.
    */
  object CommunicationType extends Enumeration {
    val UPDATES, NOTIFICATIONS, COLLABORATIONS = Value
  }

}
