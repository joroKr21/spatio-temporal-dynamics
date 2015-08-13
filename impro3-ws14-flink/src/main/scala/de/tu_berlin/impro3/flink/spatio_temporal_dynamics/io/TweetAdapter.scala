package de.tu_berlin.impro3.flink.spatio_temporal_dynamics.io

import java.text.SimpleDateFormat
import java.util.Locale

import collection.JavaConversions._

import de.tu_berlin.impro3.flink._
import spatio_temporal_dynamics.model.{ Tweet => ScalaTweet }
import model.tweet.{ Tweet => JavaTweet }

import org.apache.flink.api.common.functions.RichMapFunction

object TweetAdapter extends RichMapFunction[JavaTweet, ScalaTweet] {
  val dateFormat = new SimpleDateFormat("EEE MMM dd HH:mm:ss ZZZZZ yyyy", Locale.US)
  def map(tweet: JavaTweet) = {
    val text = tweet.getText
    val user = tweet.getUser.getId_str
    val time = dateFormat.parse(tweet.getCreated_at).getTime
    val tags = tweet.getEntities.getHashtags.toStream
      .map { _.getText.toLowerCase }
    val gps  = tweet.getCoordinates match {
      case null => None
      case some => some.getCoordinates match {
        case Array(0, 0) => None
        case Array(x, y) => Some(y, x)
        case _           => None
      }
    }

    val city = try if (tweet.getPlace.getPlace_type.toLowerCase == "city")
      Some(tweet.getPlace.getName) else None
    catch { case _: Exception => None }

    ScalaTweet(text, user, time, city, tags, gps)
  }
}
