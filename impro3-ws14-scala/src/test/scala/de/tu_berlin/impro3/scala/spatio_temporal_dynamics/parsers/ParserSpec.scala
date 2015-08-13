package de.tu_berlin.impro3.scala.spatio_temporal_dynamics.parsers

import java.util.Date

import de.tu_berlin.impro3.scala.spatio_temporal_dynamics._
import model.Tweet

import org.junit.runner.RunWith
import org.scalatest._
import org.scalatest.junit.JUnitRunner
import org.scalatest.prop.PropertyChecks

@RunWith(classOf[JUnitRunner])
class ParserSpec extends PropSpec with PropertyChecks with Matchers {
  val jsonParser = new JsonParser
  val jaxParser  = new JaxParser
  val csvParser  = new CsvParser
  val tabParser  = new TabularParser

  def toJson(tweet: Tweet) = {
    val date = jsonParser.dateFormat.get.format(new Date(tweet.time))
    val tags = tweet.tags.map { tag => s"""{"text":"$tag"}"""}.mkString(",")
    val gps  = tweet.gps match {
      case Some((lat, lon)) => s"""{"coordinates":[$lon, $lat]}"""
      case None             => "null"
    }

    s"""{"text":"${tweet.text}",
       | "user":{"id_str":"${tweet.user}"},
       | "created_at":"$date",
       | "entities":{"hashtags":[$tags]},
       | "coordinates":$gps}""".stripMargin
  }

  def toCsv(tweet: Tweet) = {
    val date = csvParser.dateFormat.get.format(new Date(tweet.time))
    val (lat, lon) = tweet.gps match {
      case Some(gps) => gps
      case None      => (0.0, 0.0)
    }

    val seq = Seq(tweet.user, date, tweet.text, 0, lon, lat)
    seq.mkString(csvParser.separator.toString)
  }

  def toTabular(tweet: Tweet) = {
    val user = "USER_" + tweet.user
    val date = tabParser.dateFormat.get.format(new Date(tweet.time))
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
      whenever(tweet.gps.isDefined) {
        val option = tabParser.parse(toTabular(tweet))
        option should be ('defined)
        option.get shouldMatchTweet tweet
      }
    }
  }
}
