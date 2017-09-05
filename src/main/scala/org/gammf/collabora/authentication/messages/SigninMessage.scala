package org.gammf.collabora.authentication.messages

import org.gammf.collabora.util.User

/**
  * Represent a signin make by the user
  * @param user the user that send the signin request.
  */
case class SigninMessage(user: User)
