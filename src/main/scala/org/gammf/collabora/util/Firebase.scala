package org.gammf.collabora.util

import org.apache.http.HttpResponse
import org.apache.http.client.methods.HttpPost
import org.apache.http.entity.StringEntity
import org.apache.http.impl.client.HttpClientBuilder
import scala.collection.mutable

/**
  * Util class to manage a Firebase HTTP request
  */
class Firebase {

  private val FIREBASE_URI = "https://fcm.googleapis.com/fcm/send"
  private val post: HttpPost = new HttpPost(FIREBASE_URI)
  private val attributes = mutable.HashMap[String,String]()

  /**
    * Method used to set server GoogleAPI Key
    * @param key the GoogleAPI Key
    */
  def setKey(key: String): Unit = {
    post.setHeader("Authorization", "key=" + key)
    post.setHeader("Content-Type", "application/json")
    attributes += ("title"->"","body"->"","topic"->"")
  }

  /**
    * Method used to set title of the notification
    * @param title the title to be setted
    */
  def setTitle(title:String): Unit = {
    attributes.update("title",title)
  }

  /**
    * Method used to set body of the notification
    * @param body the body to be setted
    */
  def setBody(body:String): Unit = {
    attributes.update("body",body)
  }

  /**
    * Method used to set topic to send the notification
    * @param toSend the topic where is sent the notification
    */
  def to(toSend:String): Unit = {
    attributes.update("topic",toSend)
  }

  /**
    * Method that send the message by HTTP post
    */
  def send(): Unit = {
    checkValues()
    val http = HttpClientBuilder.create.build
    val response:HttpResponse = http.execute(post)
    System.out.println(response)
    clear()
  }

  /**
    * Method that check that all the values (TITLE, BODY AND TOPIC) are setted
    */
  private def checkValues(): Unit = {
    if(attributes.values.exists(_=="")){
      throw new IllegalArgumentException("miss requests attributes")
    }else{
      val payload = "{\"notification\": { \"title\": \"" + attributes("title") + "\", \"body\": \"" +
        attributes("body") + "\"}, \"to\" : \"/topics/" + attributes("topic") + "\"}"
      System.out.println(payload)
      post.setEntity(new StringEntity(payload))
    }
  }

  /**
    * Method used after the send that clear all the attribute's field
    */
  private def clear(): Unit = {
    attributes += ("title"->"","body"->"","topic"->"")
  }

}

/**
  * Simple first test entry for Firebase class
  */
object UseFirebase extends App {
  var firebase:Firebase = new Firebase
  firebase.setKey("AAAAJtSw2Gk:APA91bEXmB5sRFqSnuYIP3qofHQ0RfHrAzTllJ0vYWtHXKZsMdbuXmUKbr16BVZsMO0cMmm_BWE8oLzkFcyuMr_V6O6ilqvLu7TrOgirVES51Ux9PsKfJ17iOMvTF_WtwqEURqMGBbLf")
  firebase.setTitle("TITOLONE")
  firebase.setBody("BODYESEMPIO")
  firebase.to("collabora-project-id")
  firebase.send()
}
