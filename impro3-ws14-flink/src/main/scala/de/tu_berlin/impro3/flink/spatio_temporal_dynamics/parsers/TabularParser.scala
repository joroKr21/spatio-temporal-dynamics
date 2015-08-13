package de.tu_berlin.impro3.flink.spatio_temporal_dynamics.parsers

import de.tu_berlin.impro3.flink.spatio_temporal_dynamics._
import model.Tweet

import java.text.SimpleDateFormat
import java.util.Locale

/** [[Tweet]] parser for tabular input */
class TabularParser extends Parser {
  val dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US)
  val separator = '\t'
  def parse(tabular: String) = try {
    val tweet = tabular.split(separator)
    val text  = tweet.drop(5).mkString(separator.toString)
    val user  = tweet(0).drop(5)
    val time  = dateFormat.parse(tweet(1)).getTime
    val tags  = extractHashTags(text)
    val gps   = Some(tweet(3).toDouble, tweet(4).toDouble)
    Some(Tweet(text, user, time, None, tags, gps))
  } catch { case _: Exception => None }
}
