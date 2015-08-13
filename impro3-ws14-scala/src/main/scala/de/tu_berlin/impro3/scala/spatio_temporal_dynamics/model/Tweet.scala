package de.tu_berlin.impro3.scala.spatio_temporal_dynamics.model

import de.tu_berlin.impro3.scala.spatio_temporal_dynamics._
import geo.Location

/** Representation of a tweet with some relevant metadata. */
case class Tweet(text: String, user: String, time: Long,
                 tags: Seq[String], gps: Option[(Double, Double)]) {
  /** Extract all [[HashTag]]s from this Tweet if it has a [[Location]]. */
  lazy val hashTagsWithLocation: Seq[HashTag] = gps match {
    case Some(coordinates) if geo.validMgrs(coordinates) =>
      tags.map { HashTag(_, time, Location(coordinates)) }
    case _ => Nil
  }
}

object Tweet { // companion object
  val default = Tweet("", "", 0, Nil, None)
}
