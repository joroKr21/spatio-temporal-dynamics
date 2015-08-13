package de.tu_berlin.impro3.flink.spatio_temporal_dynamics.parsers

import java.util.Date

import de.tu_berlin.impro3.flink.io.TweetInputFormat
import de.tu_berlin.impro3.flink.spatio_temporal_dynamics._
import io.TweetAdapter
import model.Tweet

import org.junit.runner.RunWith
import org.scalatest._
import org.scalatest.junit.JUnitRunner
import org.scalatest.prop.PropertyChecks

@RunWith(classOf[JUnitRunner])
class ParserSpec extends PropSpec with PropertyChecks with Matchers {
  val jsonParser = new JsonParser
  val gsonParser = new TweetInputFormat
  val jaxParser  = new JaxParser
  val csvParser  = new CsvParser
  val tabParser  = new TabularParser

  def toJson(tweet: Tweet) = {
    val date = jsonParser.dateFormat.format(new Date(tweet.time))
    val tags = tweet.tags.map { tag => s"""{"text":"$tag"}"""}.mkString(",")
    val name = tweet.city.getOrElse("unknown")
    val tpe  = if (tweet.city.isEmpty) "unknown" else "city"
    val gps  = tweet.gps match {
      case Some((lat, lon)) => s"""{"coordinates":[$lon, $lat]}"""
      case None             => "null"
    }

    s"""{"text":"${tweet.text}",
       | "user":{"id":${tweet.user},"id_str":"${tweet.user}"},
       | "created_at":"$date",
       | "entities":{"hashtags":[$tags]},
       | "place":{"place_type":"$tpe","name":"$name"},
       | "coordinates":$gps}""".stripMargin
  }

  def toCsv(tweet: Tweet) = {
    val date = csvParser.dateFormat.format(new Date(tweet.time))
    val (user, text) = (tweet.user, tweet.text)
    val (place, tpe) = tweet.city match {
      case Some(city) => city      -> "city"
      case None       => "unknown" -> "unknown"
    }

    val (lat, lon) = tweet.gps match {
      case Some(gps) => gps
      case None      => (0.0, 0.0)
    }

    val seq = Seq(user, date, text, 0, lon, lat, "", place, "", tpe)
    seq.mkString(csvParser.separator.toString)
  }

  def toTabular(tweet: Tweet) = {
    val user = "USER_" + tweet.user
    val date = tabParser.dateFormat.format(new Date(tweet.time))
    val (lat, lon) = tweet.gps match {
      case Some(gps) => gps
      case None      => (0.0, 0.0)
    }

    val seq = Seq(user, date, tweet.gps, lat, lon, tweet.text)
    seq.mkString(tabParser.separator.toString)
  }

  property("A JsonParser should parse arbitrary Tweets from JSON format") {
    forAll { tweet: Tweet =>
      val option = jsonParser.parse(toJson(tweet))
      option should be ('defined)
      option.get shouldMatchTweet tweet
    }
  }

  property("A JaxParser should parse arbitrary Tweets from JSON format") {
    forAll { tweet: Tweet =>
      val option = jaxParser.parse(toJson(tweet))
      option should be ('defined)
      option.get shouldMatchTweet tweet
    }
  }

  property("A Gson parser should parse arbitrary Tweets from JSON format") {
    forAll { tweet: Tweet =>
      val bytes  = toJson(tweet).getBytes
      val parsed = gsonParser.readRecord(null, bytes, 0, bytes.length)
      TweetAdapter.map(parsed) shouldMatchTweet tweet
    }
  }

  property("A CsvParser should parse arbitrary Tweets from CSV format") {
    forAll { tweet: Tweet =>
      whenever(tweet.gps.isDefined) {
        val option = csvParser.parse(toCsv(tweet))
        option should be ('defined)
        option.get shouldMatchTweet tweet
      }
    }
  }

  property("A TabularParser should parse arbitrary Tweets from tab. format") {
    forAll { tweet: Tweet =>
      whenever(tweet.gps.isDefined && tweet.city.isEmpty) {
        val option = tabParser.parse(toTabular(tweet))
        option should be ('defined)
        option.get shouldMatchTweet tweet
      }
    }
  }
}