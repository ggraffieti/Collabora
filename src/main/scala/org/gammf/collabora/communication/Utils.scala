package org.gammf.collabora.communication

/**
  * Created by mperuzzi on 03/08/17.
  */
object Utils {

  def fromBytes(x: Array[Byte]) = new String(x, "UTF-8")
  def toBytes(x: Long) = x.toString.getBytes("UTF-8")

}
