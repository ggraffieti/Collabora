package org.gammf.collabora

/**
  * A utility class for Database constants.
  */
package object database {

  /**
    * The name of the Database in the mongoDB server
    */
  val DB_NAME = "collabora"

  /**
    * The connection string that must be used to connect to the database on the mongoDB server
    */
  val CONNECTION_STRING: String = "mongodb://localhost:27017/" + DB_NAME + "?authMode=scram-sha1"

  /**
    * The name of the collection that contains collaborations
    */
  val COLLABORATION_CONNECTION_NAME = "collaboration"

  /**
    * The name of the collection that contains users
    */
  val USER_COLLECTION_NAME = "user"






  /**
    * The name of the field in the collaboration collection, that contains the id
    */
  val COLLABORATION_ID = "_id"

  /**
    * The name of the field in the collaboration collection, that contains the name of the collaboration
    */
  val COLLABORATION_NAME = "name"

  /**
    * The name of the field in the collaboration collection, that contains the collaboration type
    */
  val COLLABORATION_COLLABORATION_TYPE = "collaborationType"

  /**
    * The name of the field in the collaboration collection, that contains the members
    */
  val COLLABORATION_USERS = "users"

  /**
    * The name of the field in the collaboration collection, that contains the modules
    */
  val COLLABORATION_MODULES = "modules"

  /**
    * The name of the field in the collaboration collection, that contains the notes
    */
  val COLLABORATION_NOTES = "notes"







  /**
    * The name of the field in the collaboration collection, that contains the member username
    */
  val COLLABORATION_USER_USERNAME = "username"

  /**
    * The name of the field in the collaboration collection, that contains the member right
    */
  val COLLABORATION_USER_RIGHT = "right"






  /**
    * the name of the field that contains the note id
    */
  val NOTE_ID = "id"

  /**
    * the name of the field that contains the note content
    */
  val NOTE_CONTENT = "content"

  /**
    * the name of the field that contains the note state
    */
  val NOTE_STATE = "state"

  /**
    * the name of the field that contains the note expiration date
    */
  val NOTE_EXPIRATION = "expiration"

  /**
    * the name of the field that contains the note's previous notes
    */
  val NOTE_PREVIOUS_NOTES = "previousNotes"

  /**
    * the name of the field that contains the note location
    */
  val NOTE_LOCATION = "location"

  /**
    * the name of the field that contains the note module
    */
  val NOTE_MODULE = "module"





  /**
    * the name of the field that contains the note state definition (todo, doing, done etc...)
    */
  val NOTE_STATE_DEFINITION = "definition"

  /**
    * the name of the field that contains the note state responsible
    */
  val NOTE_STATE_RESPONSIBLE = "responsible"






  /**
    * the name of the field that contains the module id
    */
  val MODULE_ID = "id"

  /**
    * the name of the field that contains the module description
    */
  val MODULE_DESCRIPTION = "description"

  /**
    * the name of the field that contains the module state
    */
  val MODULE_STATE = "state"

  /**
    * the name of the field that contains the module's previous modules
    */
  val MODULE_PREVIOUS_MODULES = "previousModules"







  /**
    * The name of the field that contains the location latitude
    */
  val LOCATION_LATITUDE = "latitude"

  /**
    * The name of the field that contains the location longitude
    */
  val LOCATION_LONGITUDE = "longitude"





  /**
    * The name of the field in the users collection, that contains the id
    */
  val USER_ID = "_id"

  /**
    * The name of the field in the user collection, that contains the email
    */
  val USER_EMAIL = "email"

  /**
    * The name of the field in the user collection, that contains the name
    */
  val USER_NAME = "name"

  /**
    * The name of the field in the user collection, that contains the surname
    */
  val USER_SURNAME = "surname"

  /**
    * The name of the field in the user collection, that contains the birthday date
    */
  val USER_BIRTHDAY = "birthday"

  /**
    * The name of the field in the user collection, that contains the hashed user password
    */
  val USER_PASSWORD = "password"


}
