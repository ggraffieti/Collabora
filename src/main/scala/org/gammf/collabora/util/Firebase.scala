package org.gammf.collabora.util

import org.apache.http.HttpResponse
import org.apache.http.client.methods.HttpPost
import org.apache.http.entity.StringEntity
import org.apache.http.impl.client.HttpClientBuilder
import scala.collection.mutable

class Firebase {

  private var post: Option[HttpPost] = None
  private val attributes = mutable.HashMap[String,String]()
  private val FIREBASE_URI = "https://fcm.googleapis.com/fcm/send"

  def setKey(key: String): Unit = {
    post =  Some(new HttpPost(FIREBASE_URI))
    post.get.setHeader("Authorization", "key=" + key)
    post.get.setHeader("Content-Type", "application/json")
    attributes += ("title"->"","body"->"","topic"->"")
  }

  def setTtile(title:String): Unit = {
    attributes.update("title",title)
  }

  def setBody(body:String): Unit = {
    attributes.update("body",body)
  }

  def to(toSend:String): Unit = {
    attributes.update("topic",toSend)
  }

  def send(): Unit = {
    if(attributes.values.exists(_=="")){
      throw new IllegalArgumentException("miss requests attributes")
    }else{
      val payload = "{\"notification\": { \"title\": \"" + attributes("title") + "\", \"body\": \"" +
        attributes("body") + "\"}, \"to\" : \"/topics/" + attributes("topic") + "\"}"
      System.out.println(payload)
      post.get.setEntity(new StringEntity(payload))
    }
    val http = HttpClientBuilder.create.build
    val response:HttpResponse = http.execute(post.get)
    System.out.println(response)
  }

  def clear(): Unit = {
    attributes += ("title"->"","body"->"","topic"->"")
  }

}

object UseFirebase extends App {
  var firebase:Firebase = new Firebase
  firebase.setKey("AAAAJtSw2Gk:APA91bEXmB5sRFqSnuYIP3qofHQ0RfHrAzTllJ0vYWtHXKZsMdbuXmUKbr16BVZsMO0cMmm_BWE8oLzkFcyuMr_V6O6ilqvLu7TrOgirVES51Ux9PsKfJ17iOMvTF_WtwqEURqMGBbLf")
  firebase.setTtile("TITOLONE")
  firebase.setBody("BODYESEMPIO")
  firebase.to("collabora-project-id")
  firebase.send()
  firebase.clear()
  firebase.send()
}
