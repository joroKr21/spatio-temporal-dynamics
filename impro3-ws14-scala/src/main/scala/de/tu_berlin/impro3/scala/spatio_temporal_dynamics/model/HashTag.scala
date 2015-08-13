package de.tu_berlin.impro3.scala.spatio_temporal_dynamics.model

import de.tu_berlin.impro3.scala.spatio_temporal_dynamics._
import geo.Location

/** Representation of a HashTag occurrence with timestamp and [[Location]]. */
case class HashTag(text: String, time: Long, location: Location) {
  def zone = location.zone
}

object HashTag { // companion object
  // order HashTags by time of occurrence
  implicit val ordering: Ordering[HashTag] = Ordering.by { _.time }
}