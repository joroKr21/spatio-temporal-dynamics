package de.tu_berlin.impro3.flink

import de.tu_berlin.impro3.flink.spatio_temporal_dynamics.io._
import de.tu_berlin.impro3.flink.spatio_temporal_dynamics.metrics._
import HBaseOutputFormat._

import net.sourceforge.argparse4j.ArgumentParsers

import org.apache.flink.api.scala._
import org.apache.hadoop.hbase.client.Put

object SpatioTemporalDynamics extends scala.App {
  val description = """
      | This algorithm implements most of the metrics on Twitter hashtags
      | described in the paper "Spatio-Temporal Dynamics of Online Memes: A
      | Study of Geo-Tagged Tweets" by Krishna Y. Kamath, James Caverlee, Kyumin
      | Lee, and Zhiyuan Cheng. These metrics "examine the impact of location,
      | time, and distance on the adoption of hashtags, which is important for
      | understanding meme diffusion and information propagation."
    """.stripMargin
  // create argument parser
  val ap = ArgumentParsers
    .newArgumentParser("")
    .defaultHelp(true)
    .description(description)
  // input file
  ap.addArgument("file")
    .`type`(classOf[String])
    .dest("input")
    .help("Tweets input file")
  // input file format
  ap.addArgument("format")
    .choices("json", "jax", "csv", "tab")
    .dest("format")
    .help("Input file format")
  // minimal number of occurrences per hashtag
  ap.addArgument("hashtags-size")
    .`type`(classOf[String])
    .dest("hashtags-size")
    .help("Minimal number of occurrences per hashtag")
  // minimal number of occurrences per location
  ap.addArgument("locations-size")
    .`type`(classOf[String])
    .dest("locations-size")
    .help("Minimal number of occurrences per location")

  // parse arguments
  val ns = ap.parseArgs(args)
  // parameters
  val input    = ns.getString("input")
  val tagSize  = ns.getString("hashtags-size" ).toInt
  val locSize  = ns.getString("locations-size").toInt
  val tempInt  = 1000 * 60 * 60 * 24l
  val format   = ns.getString("format") match {
    case "tab" => new TabularInputFormat
    case "csv" => new CsvInputFormat
    case "jax" => new JaxInputFormat
    case _     => new JsonInputFormat // default
  }

  implicit val config: Map[String, String] = Map.empty
  val env    = ExecutionEnvironment.getExecutionEnvironment
  val tweets = env.readFile(format, input)
  val tags   = tweets
    .flatMap { _.hashTagsWithLocation.filter { _.location.gps != (0, 0) } }

  val byText = clusterByText(tags).filterTotalSize(tagSize)
  val byZone = clusterByZone(tags).filterTotalSize(locSize)

  byText.metric { set =>
    (Occurrences(set), Focus(set), Entropy(set), Spread(set), Lifespan(set))
  }.output(HBaseOutputFormat("Hashtags", "Locations") {
    case (tables, (tag, (occurs, (zone, lat, lon, focus),
                         entropy, spread, (first, last)))) =>
      val hashTags  = new Put(tag)
      val locations = new Put(zone)
      val metric    = toBytes("metrics")
      val hashTag   = toBytes("hashtags")

      hashTags.addColumn(metric, "occurrences", occurs )
      hashTags.addColumn(metric, "focus-zone",  zone   )
      hashTags.addColumn(metric, "focus-lat",   lat    )
      hashTags.addColumn(metric, "focus-lon",   lon    )
      hashTags.addColumn(metric, "focus",       focus  )
      hashTags.addColumn(metric, "entropy",     entropy)
      hashTags.addColumn(metric, "spread",      spread )
      hashTags.addColumn(metric, "first-occur", first  )
      hashTags.addColumn(metric, "last-occur",  last   )

      locations.addColumn(hashTag, tag, occurs)

      tables("Hashtags" ).put(hashTags)
      tables("Locations").put(locations)
  })

  byZone.symmetric(byText) { (set1, set2) =>
    ( HashTagSimilarity (set1, set2),
      AdoptionLag       (set1, set2),
      SpatialImpact     (set1, set2))
  }.output(HBaseOutputFormat("Locations") {
    case (tables, ((zoneA, zoneB), (similarity, adoptionLag, spatial))) =>
      val locA   = new Put(zoneA)
      val locB   = new Put(zoneB)
      val metric = toBytes("metrics")

      locA.addColumn(metric, s"$zoneB:similarity",     similarity )
      locA.addColumn(metric, s"$zoneB:adoption_lag",   adoptionLag)
      locA.addColumn(metric, s"$zoneB:spatial_impact", spatial    )

      locB.addColumn(metric, s"$zoneA:similarity",     similarity )
      locB.addColumn(metric, s"$zoneA:adoption_lag",   adoptionLag)
      locB.addColumn(metric, s"$zoneA:spatial_impact", -spatial   )

      tables("Locations").put(locA)
      tables("Locations").put(locB)
  })

  byText.temporal(tempInt) { set =>
    (Occurrences(set), Focus(set), Entropy(set), Spread(set))
  }.output(HBaseOutputFormat("Hashtags") {
    case (tables, (tag, (day, (occurs, (_, _, _, focus), entropy, spread)))) =>
      val hashTags = new Put(tag)
      val temporal = toBytes("temporal")
      hashTags.addColumn(temporal, s"$day:occurrences", occurs )
      hashTags.addColumn(temporal, s"$day:focus",       focus  )
      hashTags.addColumn(temporal, s"$day:entropy",     entropy)
      hashTags.addColumn(temporal, s"$day:spread",      spread )
      tables("Hashtags").put(hashTags)
  })

  byZone.metric { set =>
    (Occurrences(set), Midpoint(set))
  }.output(HBaseOutputFormat("SortedLocations") {
    case (tables, (zone, (occurs, (lat, lon)))) =>
      val sorted   = new Put(occurs)
      val location = toBytes("location")
      sorted.addColumn(location, s"$zone:latitude",  lat)
      sorted.addColumn(location, s"$zone:longitude", lon)
      tables("SortedLocations").put(sorted)
  })

  val coarse: Cluster = tags
    .groupBy { _.text }
    .reduceGroup { tags =>
      val seq = tags.toStream
      seq.head.text -> seq.groupBy { _.location.mgrs(1e5.toInt) }
    }

  coarse.metric { set =>
    (Midpoint.distribution(set), Occurrences.distribution(set))
  }.output(HBaseOutputFormat("SortedHashtags", "HashtagDistribution") {
    case (tables, (tag, (midpoints, distribution))) =>
      val occurs   = distribution.values.sum
      val sorted   = new Put(occurs)
      val hashTags = new Put(tag)
      val hashTag  = toBytes("hashtags")
      val location = toBytes("locations")

      sorted.addColumn(hashTag, tag, "")

      for { (zone, occ) <- distribution } {
        val (lat, lon) = midpoints(zone)
        hashTags.addColumn(location, s"$zone:occurrences", occ)
        hashTags.addColumn(location, s"$zone:latitude",    lat)
        hashTags.addColumn(location, s"$zone:longitude",   lon)
      }

      tables("SortedHashtags"     ).put(sorted)
      tables("HashtagDistribution").put(hashTags)
  })

  val result = env.execute("Spatio-Temporal Dynamics of Online Memes")
  println(s"Time: ${result.getNetRuntime} ms")
}