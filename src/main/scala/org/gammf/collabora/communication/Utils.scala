package org.gammf.collabora.communication

/**
  * @author Manuel Peruzzi
  * This is a simple utility class.
  */
object Utils {

  def fromBytes(x: Array[Byte]) = new String(x, "UTF-8")
  def toBytes(x: Long) = x.toString.getBytes("UTF-8")

}
