package sandbox.utils

import play.api.libs.json._

import java.sql.{Date, Timestamp}

object ColumnTypeConversion {
  implicit val dateWrites: Writes[Date] = (date: Date) => JsNumber(date.getTime)
  implicit val timestampWrites: Writes[Timestamp] = (timestamp: Timestamp) => JsNumber(timestamp.getTime)

  // Extend this object for additional type conversions if needed
}