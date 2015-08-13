package de.tu_berlin.impro3.scala.spatio_temporal_dynamics.parsers

import java.util.{ TimeZone, GregorianCalendar }

import de.tu_berlin.impro3.scala.spatio_temporal_dynamics._
import model.Tweet

import scala.io.Source

import org.junit.runner.RunWith
import org.scalatest._
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class ParserTest extends FlatSpec with Matchers {
  val jsonParser = new JsonParser
  val jaxParser  = new JaxParser
  val csvParser  = new CsvParser
  val tabParser  = new TabularParser
  val jsonFile   = "tweets.random-001000.json"

  "A JsonParser and a JaxParser" should "have the same output" in {
    val lines   = Source.fromFile {
      getClass.getClassLoader.getResource(jsonFile).getPath
    }.getLines().toSeq.par
    val total   = lines.size
    val tweetsL = jsonParser.parse(lines)
    val tweetsR =  jaxParser.parse(lines)
    tweetsL should have size total
    tweetsR should have size total
    tweetsL.zip(tweetsR).foreach {
      case (left, right) => left shouldMatchTweet right
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


    val tweet   = Tweet(text, user, time, tags, gps)
    val optionL = jsonParser.parse(json)
    val optionR =  jaxParser.parse(json)
    optionL should be ('defined)
    optionR should be ('defined)
    optionL.get shouldMatchTweet tweet
    optionR.get shouldMatchTweet tweet
  }

  it should "fail on invalid date format" in {
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
    val tags   = "newyear" :: Nil
    val lat    = 12.3456
    val lon    = 65.4321
    val gps    = Some(lat, lon)
    val csv    = s"$user|01/01/2015 00:00:01|$text|0|$lon|$lat|extra|fields"
    val option = csvParser.parse(csv)
    option should be ('defined)
    option.get shouldMatchTweet Tweet(text, user, time, tags, gps)
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
