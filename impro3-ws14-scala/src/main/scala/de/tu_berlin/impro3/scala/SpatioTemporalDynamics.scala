package de.tu_berlin.impro3.scala

import java.lang.{ Long => JLong }
import java.io._

import net.sourceforge.argparse4j.ArgumentParsers
import net.sourceforge.argparse4j.inf._

import de.tu_berlin.impro3.scala.spatio_temporal_dynamics._
import metrics._
import parsers._

import scala.io.Source
import language.reflectiveCalls

/**
 * This algorithm implements most of the metrics on Twitter hashtags described
 * in the paper "Spatio-Temporal Dynamics of Online Memes: A Study of Geo-Tagged
 * Tweets" by Krishna Y. Kamath, James Caverlee, Kyumin Lee, and Zhiyuan Cheng.
 * These metrics "examine the impact of location, time, and distance on the
 * adoption of hashtags, which is important for understanding meme diffusion and
 * information propagation."
 */
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
  // output directory
  ap.addArgument("dir")
    .`type`(classOf[String])
    .dest("output")
    .help("Output directory")
  // input file format
  ap.addArgument("-f", "--format")
    .choices("json", "jax", "csv", "tab")
    .default("json")
    .dest("format")
    .help("Input file format")
  // minimal number of occurrences filter
  ap.addArgument("-m", "--min-size")
    .`type`(classOf[Integer])
    .default(1)
    .dest("min-size")
    .help("Minimal number of occurrences filter")
  // time interval for temporal metrics
  ap.addArgument("-t", "--time-int")
    .`type`(classOf[JLong])
    .default(1000 * 60 * 60 * 24l)
    .dest("time-int")
    .help("Time interval for temporal metrics in ms")
  // parse arguments
  val ns = try {
    ap.parseArgs(args)
  } catch { case ape: ArgumentParserException =>
    ap.handleError(ape)
    sys.exit(1)
  }
  // parameters
  val inputFile = ns.getString("input")
  val outputDir = ns.getString("output")
  val timeInt   = ns.getLong("time-int")
  val minSize   = ns.getInt ("min-size")
  val parser    = ns.getString("format") match {
    case "tab" => new TabularParser
    case "csv" => new CsvParser
    case "jax" => new JaxParser
    case _     => new JsonParser // default
  }
  // clustering
  lazy val lines  = Source.fromFile(inputFile).getLines().toSeq.par
  lazy val tweets = parser.parse(lines).par
  lazy val tags   = tweets.flatMap { _.hashTagsWithLocation }
  lazy val texts  = clusterByText(tags)
  lazy val zones  = clusterByZone(tags)
  lazy val byText = texts.filterTotalSize(minSize)
  lazy val byZone = zones.filterTotalSize(minSize)

  byText.metric { set =>
    (Occurrences(set), Focus(set), Entropy(set), Spread(set), Lifespan(set))
  }.toStream.sortBy { _._1 }.writeLines(outputDir, "hashtags.csv") {
    case (tag, (occurs, (zone, focus), entropy, spread, (first, last))) =>
      Seq(tag, occurs, zone, focus, entropy, spread, first, last)
        .mkString(",")
  } // unary metrics

  byText.temporal(timeInt) { set =>
    (Occurrences(set), Focus(set), Entropy(set), Spread(set))
  }.toStream.sortBy { _._1 }.writeLines(outputDir, "temporal.csv") {
    case ((tag, time), (occurs, (zone, focus), entropy, spread)) =>
      Seq(tag, time, occurs, zone, focus, entropy, spread).mkString(",")
  } // temporal metrics

  byZone.metric { (set1, set2) =>
    ( HashTagSimilarity(set1, set2),
      AdoptionLag      (set1, set2),
      SpatialImpact    (set1, set2))
  }.toStream.sortBy { _._1 }.writeLines(outputDir, "locations.csv") {
    case ((zoneA, zoneB), (similarity, adoptionLag, spatialImpact)) =>
      Seq(zoneA, zoneB, similarity, adoptionLag, spatialImpact).mkString(",")
  } // binary metrics

  println(s"Time: ${System.currentTimeMillis - executionStart} ms")

  /** Write to file as separate lines. */
  implicit class WriteLines[T](lines: Seq[T]) {
    def writeLines(dir: String, file: String)(tos: T => String) = {
      val path   = new File(dir).toPath.resolve(file)
      val writer = new PrintWriter(path.toFile)
      try lines.foreach { l => writer.println(tos(l)) }
      finally writer.close()
    }
  }

  /** Disambiguate argeparse4j interop problem through reflection. */
  implicit class Default(arg: {
    def setDefault(value: Any): Argument
  }) { def default(value: Any) = arg.setDefault(value) }
}
