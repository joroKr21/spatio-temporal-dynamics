package de.tu_berlin.impro3.flink.spatio_temporal_dynamics.parsers

import java.util.{ TimeZone, GregorianCalendar }

import de.tu_berlin.impro3.flink.io.TweetInputFormat
import de.tu_berlin.impro3.flink.spatio_temporal_dynamics._
import io.TweetAdapter
import model.Tweet

import scala.io.Source

import org.junit.runner.RunWith
import org.scalatest._
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class ParserTest extends FlatSpec with Matchers {
  val jsonParser = new JsonParser
  val gsonParser = new TweetInputFormat
  val jaxParser  = new JaxParser
  val csvParser  = new CsvParser
  val tabParser  = new TabularParser
  val jsonFile   = "tweets.random-001000.json"

  "A JsonParser, JaxParser and Gson parser" should "have the same output" in {
    val lines   = Source.fromFile {
      getClass.getClassLoader.getResource(jsonFile).getPath
    }.getLines().toStream
    val total   = lines.size
    val tweets1 = jsonParser.parse(lines)
    val tweets2 =  jaxParser.parse(lines)
    val tweets3 = lines.map { line =>
      val bytes = line.getBytes
      val tweet = gsonParser.readRecord(null, bytes, 0, bytes.length)
      TweetAdapter.map(tweet)
    } // size should be the same
    tweets1 should have size total
    tweets2 should have size total
    tweets3 should have size total
    // and all tweets should match
    tweets1.zip(tweets2).zip(tweets3).foreach {
      case ((tweet1, tweet2), tweet3) =>
        tweet1 shouldMatchTweet tweet2
        tweet2 shouldMatchTweet tweet3
    }
  }

  it should "parse JSON input with extra fields" in {
    val text = "Happy #newyear 2015!"
    val user = "12345678"
    val date = new GregorianCalendar(TimeZone.getTimeZone("GMT"))
    date.set(2015, 0, 1, 0, 0, 1)
    val time = date.getTimeInMillis
    val tags = "newyear" :: Nil
    val lat  = 12.3456
    val lon  = 65.4321
    val gps  = Some(lat, lon)
    val json = s"""{
        | "text":"$text",
        | "user":{"id":$user,"id_str":"$user"},
        | "created_at":"Thu Jan 01 00:00:01 +0000 2015",
        | "entities":{"hashtags":[{"text":"newyear","foo":"bar"}]},
        | "coordinates":{"coordinates":[$lon,$lat],"type":"Point"},
        | "some":{"other":"field"}
        |}""".stripMargin


    val tweet   = Tweet(text, user, time, None, tags, gps)
    val bytes   = json.getBytes
    val option1 = jsonParser.parse(json)
    val option2 =  jaxParser.parse(json)
    val parsed3 = gsonParser.readRecord(null, bytes, 0, bytes.length)
    option1 should be ('defined)
    option2 should be ('defined)
    option1.get               shouldMatchTweet tweet
    option2.get               shouldMatchTweet tweet
    TweetAdapter.map(parsed3) shouldMatchTweet tweet
  }

  "A JsonParser and JaxParser" should "fail on invalid date format" in {
    val json = """{"created_at":"01/01/2015 00:00:01"}"""
    jsonParser.parse(json) should be (None)
     jaxParser.parse(json) should be (None)
  }

  it should "fail on invalid JSON input" in {
    jsonParser.parse("}") should be (None)
     jaxParser.parse("}") should be (None)
  }

  it should "fail on missing JSON fields" in {
    jsonParser.parse("{}") should be (None)
     jaxParser.parse("{}") should be (None)
  }

  "A CsvParser" should "parse CSV input with extra fields" in {
    val text   = "Happy #newyear 2015!"
    val user   = "username"
    val time   = new GregorianCalendar(2015, 0, 1, 0, 0, 1).getTimeInMillis
    val date   = "01/01/2015 00:00:01"
    val city   = "New York"
    val tags   = "newyear" :: Nil
    val lat    = 12.3456
    val lon    = 65.4321
    val gps    = Some(lat, lon)
    val csv    = s"$user|$date|$text|0|$lon|$lat|US|$city||city|extra|fields"
    val option = csvParser.parse(csv)
    option should be ('defined)
    option.get shouldMatchTweet Tweet(text, user, time, Some(city), tags, gps)
  }

  it should "fail on missing fields" in {
    val csv = "this|01/01/2015 00:00:01|should|fail"
    csvParser.parse(csv) should be (None)
  }

  it should "fail on invalid date format" in {
    val csv = "user|01/01/2015-00:00:01|text|0|0.0|0.0"
    csvParser.parse(csv) should be (None)
  }

  "A TabularParser" should "fail on missing fields" in {
    val tabular = "USER_12345678\t2015-01-01T00:00:01\ttext"
    tabParser.parse(tabular) should be (None)
  }

  it should "fail on invalid date format" in {
    val tabular = "USER_12345678\t2015/01/01-00:00:01\tna\t0.0\t0.0\ttext"
    csvParser.parse(tabular) should be (None)
  }
}
