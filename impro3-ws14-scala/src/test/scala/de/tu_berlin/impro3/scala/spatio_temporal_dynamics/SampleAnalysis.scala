package de.tu_berlin.impro3.scala.spatio_temporal_dynamics

import metrics._
import parsers.TabularParser

import scala.io.Source

import java.nio.charset.CodingErrorAction
import scala.io.Codec

/**
 * Here we use all the metrics to analyze a sample of geo-tagged tweets. You
 * can download the data set from [[http://www.ark.cs.cmu.edu/GeoText here]]
 */
object SampleAnalysis extends App {
  implicit val codec = Codec("UTF-8")
  codec.onMalformedInput     (CodingErrorAction.REPLACE)
  codec.onUnmappableCharacter(CodingErrorAction.REPLACE)
  def hours(millis: Double) = millis / (1e3 * 60 * 60)
  val defaultFile = getClass.getClassLoader.getResource("tmp-sample.txt")
  val file        = if (args.isEmpty) defaultFile.getPath else args(0)
  lazy val lines  = Source.fromFile(file).getLines().toSeq.par
  lazy val tweets = new TabularParser().parse(lines).par
  lazy val tags   = tweets.flatMap { _.hashTagsWithLocation }
  lazy val texts  = clusterByText(tags)
  lazy val zones  = clusterByZone(tags)
  lazy val byText = texts.filterTotalSize(50)
  lazy val byZone = zones.filterTotalSize(50)
  lazy val unary  = byText.metric { set =>
    (Occurrences(set), Focus(set), Entropy(set), Spread(set), Lifespan(set))
  } // unary metrics
  lazy val temp   = byText.temporal(1000 * 60 * 60 * 24) { set =>
    (Occurrences(set), Focus(set), Entropy(set), Spread(set))
  } // temporal metrics
  lazy val binary = byZone.metric { (set1, set2) =>
    ( HashTagSimilarity(set1, set2),
      hours(AdoptionLag(set1, set2)),
      SpatialImpact    (set1, set2))
  } // binary metrics
  // print results
  println(s"tweets:             ${tweets.size}")
  println(s"hash-tags:          ${  tags.size}")
  println(s"unique   hash-tags: ${ texts.size}")
  println(s"filtered hash-tags: ${byText.size}")
  println(s"unique   locations: ${ zones.size}")
  println(s"filtered locations: ${byZone.size}")
  print  ("\n\n(tag, (occurrences, (zone, focus), ")
  println("entropy, spread, (first, last)))\n")
   unary.take(50).toStream.sortBy { _._1 }.foreach(println)
  println("\n\n((tag, day), (occurrences, (zone, focus), entropy, spread))\n")
    temp.take(50).toStream.sortBy { _._1 }.foreach(println)
  print  ("\n\n((zoneA, zoneB), ")
  println("(similarity, adoption lag [hrs], spatial impact))\n")
  binary.take(50).toStream.sortBy { _._1 }.foreach(println)
  println(s"\n\ntime: ${System.currentTimeMillis - executionStart} ms")
}
