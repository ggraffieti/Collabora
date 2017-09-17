package org.gammf.collabora

import org.joda.time.DateTime

/**
  *
  * This object is a test util
  * include also keyword for:
  * -encoding used for strings
  * -type of messages
  * -time for timeout, intervals, tasks, ..
  */
object TestUtil {

  val TYPE_COLLABORATIONS = "collaborations"
  val TYPE_UPDATES = "updates"
  val TYPE_NOTIFICATIONS = "notifications"

  val SERVER_UPDATE = "update.server"
  val ROUTING_KEY_EMPTY = ""

  val COLLABORATION_ROUTING_KEY = "fone"
  val NOTIFICATIONS_ROUTING_KEY = "59804868f27da3fcfe0a8e20"
  val ROUTING_KEY = "59806a4af27da3fcfe0ac0ca"

  val BROKER_HOST = "localhost"

  val TIMEOUT_SECOND = 4
  val INTERVAL_MILLIS = 100
  val TASK_WAIT_TIME = 5

  val CHARSET = "charset"
  val STRING_ENCODING = "UTF-8"

  val START_FOR_INDEX = 1
  val FINAL_FOR_INDEX = 5

  val FAKE_ID = "59806a4af27da3fcfe0ac0ca"
  val USER_ID = "maffone"
  val USER_MAIL = "maffone@collabora.com"
  val NAME = "alf"
  val SURNAME = "redo"
  val DATE_BIRTHDAY: DateTime = DateTime.parse("2000-07-10T18:24:18Z")
  val HASHED_PWD = "f4KeH4sHèDP4sSw0rD!!"

  val MESSAGE_LENGTH = 5

  val SIGNIN_ACTION = "/signin"
  val LOGIN_ACTION = "/login"
  val HTTP_LOGIN = "login"
  val HTTP_BASIC_CHALLENGE = "Basic"
  val CORRECT_PASSWORD = "admin"
  val WRONG_PASSWORD = "not_maffone_password"
  val WRONG_USERNAME = "wrong_username"
  val PASSWORD = "password"

}
