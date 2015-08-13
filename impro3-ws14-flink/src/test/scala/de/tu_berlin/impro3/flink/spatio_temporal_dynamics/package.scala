package de.tu_berlin.impro3.flink

import spatio_temporal_dynamics.model._
import spatio_temporal_dynamics.parsers.Parser
import spatio_temporal_dynamics.geo.Location

import org.apache.flink.api.java.io.LocalCollectionOutputFormat
import org.apache.flink.api.scala._

import org.scalacheck.{ Gen, Arbitrary => Arb }
import org.scalatest.Matchers._
import org.scalatest.matchers.{ BeMatcher, MatchResult }
import Gen._

import collection.mutable
import collection.JavaConversions._

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
    (text: String, user: Long, time: Long, city: Option[String],
     gps: Option[(Double, Double)]) =>
      Tweet(text, user.toString, time, city, parser.extractHashTags(text), gps)
  } (Arb(txtGen), Arb(posNum[Long]), Arb(posNum[Long]), Arb(option(alphaStr)),
     Arb(option(gpsGen))))

  implicit val arbHashTag: Arb[HashTag] = Arb(resultOf {
    (text: String, time: Long, city: Option[String], gps: (Double, Double)) =>
      HashTag(text, time, Location(gps), city)
  } (Arb(alphaNumStr), Arb(posNum[Long]), Arb(option(alphaStr)), Arb(mgrsGen)))

  implicit class MatchTweet(left: Tweet) {
    def shouldMatchTweet(right: Tweet): Unit = {
      left.tags.sorted should equal (right.tags.sorted)
      left.time        should equal (right.time +- 999)
      left.city        should equal (right.city)
      left.text        should equal (right.text)
      left.user        should equal (right.user)
      left.gps         should equal (right.gps )
    }
  }

  def withDataSet[T](coll: Seq[HashTag])
                    (test: DataSet[HashTag] => DataSet[T]) = {
    val env = ExecutionEnvironment.createCollectionsEnvironment
    val buf = mutable.Buffer.empty[T]
    val out = new LocalCollectionOutputFormat[T](buf)
    test(env.fromCollection(coll)).output(out)
    env.execute("Spatio-Temporal Dynamics metric test")
    buf
  }

  object ValidGps extends BeMatcher[(Double, Double)] {
    def apply(gps: (Double, Double)) = MatchResult(
      geo.validGps(gps),
      s"$gps were valid GPS coordinates",
      s"$gps were invalid GPS coordinates"
    )
  }
}
