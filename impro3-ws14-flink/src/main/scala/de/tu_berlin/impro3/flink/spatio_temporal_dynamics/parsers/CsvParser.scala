package de.tu_berlin.impro3.flink.spatio_temporal_dynamics.parsers

import de.tu_berlin.impro3.flink.spatio_temporal_dynamics._
import model.Tweet

import java.text.SimpleDateFormat
import java.util.Locale

/** [[Tweet]] parser for CSV formatted input. */
class CsvParser extends Parser {
  val dateFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss", Locale.US)
  val separator = '|'
  def parse(csv: String) = try {
    val tweet = csv.split(separator)
    val text  = tweet(2)
    val user  = tweet(0)
    val time  = dateFormat.parse(tweet(1)).getTime
    val tags  = extractHashTags(text)
    val gps   = Some(tweet(5).toDouble, tweet(4).toDouble)
    val city  = if (tweet(9).toLowerCase == "city") Some(tweet(7)) else None
    Some(Tweet(text, user, time, city, tags, gps))
  } catch { case _: Exception => None }
}
