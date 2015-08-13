package de.tu_berlin.impro3.spark.spatio_temporal_dynamics

import de.tu_berlin.impro3.spark.spatio_temporal_dynamics.parsers.TabularParser
import metrics._
import org.apache.spark._

/**
 * Here we use all the metrics to analyze a sample of geo-tagged tweets. You
 * can download the data set from [[http://www.ark.cs.cmu.edu/GeoText here]]
 */
object SampleAnalysis extends App {
  val name   = "Spatio-Temporal Dynamics metric test"
  val conf   = new SparkConf().setAppName(name).setMaster("local")
  val sc     = new SparkContext(conf)
  val input  = getClass.getClassLoader.getResource("tmp-sample.txt").getPath
  val parser = new TabularParser
  val seed   = System.currentTimeMillis
  val sample = sc.textFile(input).sample(withReplacement = false, 0.03, seed)
  val tweets = sample.flatMap(parser.parse)
  val tags   = tweets.flatMap { _.hashTagsWithLocation }
  def hours(millis: Double) = { millis / (1e3 * 60 * 60) }
  val unary  = measureByText(tags) { set =>
    (Occurrences(set), Focus(set), Entropy(set), Spread(set))
  }.collect().take(50)
  val binary = measureByZone(tags) { (set1, set2) =>
    ( HashTagSimilarity (set1, set2),
      hours(AdoptionLag (set1, set2)),
      SpatialImpact     (set1, set2))
  }.collect().take(50)
  // print results
  println("\n\n(tag, (occurrences, (zone, focus), entropy, spread))\n")
  unary .foreach(println)
  println("\n\n((zoneA, zoneB), (similarity, adoption lag, spatial impact))\n")
  binary.foreach(println)
  println(s"\n\nTime: ${System.currentTimeMillis - seed}")
}
