package org.gammf.collabora

import org.joda.time.DateTime

/**
  * Created by Mattia on 04/09/2017.
  */
object TestUtil {

  val TYPE_COLLABORATIONS = "collaborations"
  val TYPE_UPDATES = "updates"
  val TYPE_NOTIFICATIONS = "notifications"

  val SERVER_UPDATE = "update.server"
  val ROUTING_KEY_EMPTY = ""

  val COLLABORATION_ROUTING_KEY = "maffone"
  val NOTIFICATIONS_ROUTING_KEY = "59804868f27da3fcfe0a8e20"
  val ROUTING_KEY = "59806a4af27da3fcfe0ac0ca"

  val BROKER_HOST = "localhost"

  val TIMEOUT_SECOND = 4
  val INTERVAL_MILLIS = 100;
  val TASK_WAIT_TIME = 5
  val STRING_ENCODING = "UTF-8"


  val FAKE_ID = "59806a4af27da3fcfe0ac0ca"
  val FAKE_USER_ID = "maffone"
  val FAKE_USER_MAIL = "maffone@collabora.com"
  val FAKE_NAME = "alf"
  val FAKE_SURNAME = "redo"
  val FAKE_DATE_BIRTHDAY = DateTime.parse("2000-07-10T18:24:18Z")
  val FAKE_HASHED_PWD = "f4KeH4sHèDP4sSw0rD!!"

  val MESSAGE_LENGTH = 5

}
