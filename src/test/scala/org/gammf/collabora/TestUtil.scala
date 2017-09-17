package org.gammf.collabora

/**
  *
  * This object is a test util
  * include also keyword for:
  * - encoding used for strings
  * - type of messages
  * - time for timeout, intervals, tasks, ..
  */
object TestUtil {

  val TYPE_COLLABORATIONS = "collaborations"
  val TYPE_UPDATES = "updates"
  val TYPE_NOTIFICATIONS = "notifications"

  val SERVER_UPDATE = "update.server"

  val COLLABORATION_ROUTING_KEY = "fone"

  val BROKER_HOST = "localhost"

  val TIMEOUT_SECOND = 4
  val INTERVAL_MILLIS = 100
  val TASK_WAIT_TIME = 5

  val CHARSET = "charset"
  val STRING_ENCODING = "UTF-8"


  val FAKE_ID = "59806a4af27da3fcfe0ac0ca"
  val USER_ID = "maffone"

  val SIGNIN_ACTION = "/signin"
  val LOGIN_ACTION = "/login"
  val HTTP_LOGIN = "login"
  val HTTP_BASIC_CHALLENGE = "Basic"
  val CORRECT_PASSWORD = "admin"
  val WRONG_PASSWORD = "not_maffone_password"
  val WRONG_USERNAME = "wrong_username"
}
