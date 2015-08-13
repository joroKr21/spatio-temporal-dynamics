package de.tu_berlin.impro3.scala

import de.tu_berlin.impro3.scala.spatio_temporal_dynamics._
import spatio_temporal_dynamics.model.{ HashTag, Tweet }
import parsers.Parser
import geo.Location

import org.scalacheck.{ Gen, Arbitrary => Arb }
import org.scalatest.Matchers._
import Gen._

package object spatio_temporal_dynamics {
  val alphaNumStr = listOf(alphaNumChar).map { _.mkString }
  val parser  = new Parser { def parse(text: String) = None }
  val latGen  = choose( -90.0,  90.0)
  val lonGen  = choose(-180.0, 180.0)
  val gpsGen  = zip(latGen, lonGen)
  val mgrsGen = zip(choose(-80.0, 84.0), lonGen)
  val tagGen  = alphaNumStr.map { '#' + _ }
  val txtGen  = listOf(Gen.oneOf(alphaNumStr, tagGen)).map { _.mkString(" ") }

  implicit val arbLocation: Arb[Location] = Arb(resultOf {
    gps: (Double, Double) => Location(gps)
  } (Arb(gpsGen)))

  implicit val arbTweet: Arb[Tweet] = Arb(resultOf {
    (text: String, user: String, time: Long, gps: Option[(Double, Double)]) =>
      Tweet(text, user, time, parser.extractHashTags(text), gps)
  } (Arb(txtGen), Arb(alphaNumStr), Arb(posNum[Long]), Arb(option(gpsGen))))

  implicit val arbHashTag: Arb[HashTag] = Arb(resultOf {
    (text: String, time: Long, gps: (Double, Double)) =>
      HashTag(text, time, Location(gps))
  } (Arb(alphaNumStr), Arb(posNum[Long]), Arb(mgrsGen)))

  implicit class MatchTweet(left: Tweet) {
    def shouldMatchTweet(right: Tweet): Unit = {
      left.tags.sorted should equal (right.tags.sorted)
      left.time        should equal (right.time +- 999)
      left.text        should equal (right.text)
      left.user        should equal (right.user)
      left.gps         should equal (right.gps )
    }
  }
}
