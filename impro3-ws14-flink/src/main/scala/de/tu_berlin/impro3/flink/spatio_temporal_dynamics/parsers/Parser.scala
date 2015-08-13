package de.tu_berlin.impro3.flink.spatio_temporal_dynamics.parsers

import de.tu_berlin.impro3.flink.spatio_temporal_dynamics._
import model.{ HashTag, Tweet }

/**
 * Abstract [[Tweet]] parser. [[Tweet]] parsers are not intended to be
 * thread-safe and thus clients should use a separate instance per thread.
 */
trait Parser extends Serializable {
  val hashTagRegex = """\B#[\p{L}_\p{N}]*[\p{L}_]+[\p{L}_\p{N}]*""".r
  /** Extract all [[HashTag]]s from a [[String]] as a sequence. */
  def extractHashTags(text: String) =
    hashTagRegex.findAllIn(text).toStream.map { _.tail.toLowerCase }
  /** Possibly parse a single [[Tweet]]. */
  def parse(text: String): Option[Tweet]
  /** Parse a sequence of [[Tweet]]s. */
  def parse(tweets: Seq[String]): Seq[Tweet] =
    tweets.map(parse).flatten
}
