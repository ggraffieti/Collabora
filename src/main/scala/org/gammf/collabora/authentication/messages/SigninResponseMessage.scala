package org.gammf.collabora.authentication.messages

/**
  * Represents a response to a SigninMessage. ok is true if everithing gone well, false otherwise (the username given is already present
  * in the DB)
  * @param ok true if everithing gone well, and the user is correctly inserted in the DB, false otherwise.
  */
case class SigninResponseMessage(ok: Boolean)
