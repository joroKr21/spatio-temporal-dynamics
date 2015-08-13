package de.tu_berlin.impro3.scala.spatio_temporal_dynamics.parsers

import de.tu_berlin.impro3.scala.spatio_temporal_dynamics._
import model.Tweet

import java.text.SimpleDateFormat
import java.util.Locale

/** [[Tweet]] parser for CSV formatted input. */
class CsvParser extends Parser {
  val separator  = '|'
  val dateFormat = new ThreadLocal[SimpleDateFormat] {
    override def initialValue() =
      new SimpleDateFormat("MM/dd/yyyy HH:mm:ss", Locale.US)
  }

  def parse(csv: String) = try {
    val tweet = csv.split(separator)
    val text  = tweet(2)
    val user  = tweet(0)
    val time  = dateFormat.get.parse(tweet(1)).getTime
    val tags  = extractHashTags(text)
    val gps   = Some(tweet(5).toDouble, tweet(4).toDouble)
    Some(Tweet(text, user, time, tags, gps))
  } catch { case _: Exception => None }
}
