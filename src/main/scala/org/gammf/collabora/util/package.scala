package org.gammf.collabora

import org.gammf.collabora.communication.messages.PublishNotificationMessage
import org.gammf.collabora.database.messages._
import org.gammf.collabora.util.UpdateMessageType.UpdateMessageType
import org.joda.time.DateTime
import org.joda.time.format.{DateTimeFormat, ISODateTimeFormat}
import play.api.libs.json.{JsString, Reads, Writes}
import reactivemongo.bson.{BSONDateTime, BSONHandler}

package object util {
  private val jodaDateReads = Reads[DateTime](js =>
    js.validate[String].map[DateTime](dtString =>
      DateTime.parse(dtString, DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZZ"))
    )
  )
  implicit val dateReads: Reads[DateTime] = jodaDateReads

  private val jodaDateWrites: Writes[DateTime] = (date) => JsString(date.toString(ISODateTimeFormat.dateTime()))

  implicit val dateWrites: Writes[DateTime] = jodaDateWrites

  implicit object BSONDateTimeHandler extends BSONHandler[BSONDateTime, DateTime] {
    def read(time: BSONDateTime) = new DateTime(time.value)
    def write(jdtime: DateTime) = BSONDateTime(jdtime.getMillis)
  }
}
