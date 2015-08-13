package de.tu_berlin.impro3.spark.spatio_temporal_dynamics.parsers

import de.tu_berlin.impro3.spark.spatio_temporal_dynamics._
import model.Tweet

import java.text.SimpleDateFormat
import java.lang.{ ThreadLocal => JThreadLocal }
import java.util.Locale

import org.json4s._
import native.JsonMethods

/** [[Tweet]] parser for JSON formatted input. */
class JsonParser extends Parser {
  val dateFormat = new JThreadLocal[SimpleDateFormat] {
    override def initialValue() =
      new SimpleDateFormat("EEE MMM dd HH:mm:ss ZZZZZ yyyy", Locale.US)
  }

  def parse(json: JValue) = for {
    JString(text) <- json \ "text"
    JString(createdAt) <- json \ "created_at"
    time = dateFormat.get.parse(createdAt).getTime
    JString(user) <- json \ "user" \ "id_str"
    JArray(hashTags) <- json \ "entities" \ "hashtags"
    tags = for {
      tag <- hashTags
      JString(text) <- tag \ "text"
    } yield text.toLowerCase
    gps  = json \ "coordinates" \ "coordinates" match {
      case JArray(JDouble(lon) :: JDouble(lat) :: Nil) => Some(lat, lon)
      case _                                           => None
    }
  } yield Tweet(text, user, time, tags, gps)

  /** Possibly parse a single [[Tweet]]. */
  def parse(text: String) = try {
    parse(JsonMethods.parse(text)) match {
      case (tweet: Tweet) :: _ => Some(tweet)
      case _                   => None
    }
  } catch { case _: Exception => None }
}
