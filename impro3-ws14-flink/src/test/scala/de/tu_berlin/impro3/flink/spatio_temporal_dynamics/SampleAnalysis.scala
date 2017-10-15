package de.tu_berlin.impro3.flink.spatio_temporal_dynamics

import de.tu_berlin.impro3.flink.spatio_temporal_dynamics.io._
import de.tu_berlin.impro3.flink.spatio_temporal_dynamics.metrics._
import org.apache.flink.api.scala._
import org.apache.log4j.BasicConfigurator

/**
 * Here we use all the metrics to analyze a sample of geo-tagged tweets. You
 * can download the data set from [[http://www.ark.cs.cmu.edu/GeoText here]]
 */
object SampleAnalysis extends App {
  BasicConfigurator.configure()
  // values
  val env    = ExecutionEnvironment.getExecutionEnvironment
  val file   = getClass.getClassLoader.getResource("tmp-sample.txt")
  val input  = if (args.isEmpty) "file://" + file.getPath else args.head
  val tweets = env.readFile(new TabularInputFormat, input)
  val tags   = tweets.flatMap { _.hashTagsWithLocation }
  val byText = clusterByText(tags).filterTotalSize(50)
  val byZone = clusterByZone(tags).filterTotalSize(50)
  def hours(millis: Long) = { millis / (1000 * 60 * 60) }
  // unary metrics per HashTag
  byText.metric { set =>
    (Occurrences(set), Focus(set), Entropy(set), Spread(set), Lifespan(set))
  }.first(50).output(new HashTagsOutputFormat)
  // binary metrics per zone pair
  byZone.symmetric(byText) { (set1, set2) =>
    ( HashTagSimilarity (set1, set2),
      hours(AdoptionLag (set1, set2)),
      SpatialImpact     (set1, set2))
  }.first(50).output(new LocationsOutputFormat)
  // temporal metrics per HashTag per day
  byText.temporal(1000 * 60 * 60 * 24) { set =>
    (Occurrences(set), Focus(set), Entropy(set), Spread(set))
  }.first(50).output(new HashTagsOutputFormat)
  // Occurrences
  byZone.metric { set =>
    (Occurrences(set), Midpoint(set))
  }.output(new SortedLocationsOutputFormat)
  // Sorted HashTags
  val coarse: Cluster = tags.groupBy { _.text }.reduceGroup { tags =>
    val seq = tags.toStream
    seq.head.text -> seq.groupBy { _.location.mgrs(1e5.toInt) }
  }

  coarse.filterTotalSize(50).metric { set =>
    (Occurrences(set), Midpoint(set))
  }.output(new SortedHashtagsOutputFormat)

  // execute and print results
  val result = env.execute("Spatio-Temporal Dynamics sample analysis")
  println(s"\n\nTime: ${result.getNetRuntime}")
}
