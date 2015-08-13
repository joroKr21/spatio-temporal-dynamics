package de.tu_berlin.impro3.scala.spatio_temporal_dynamics.geo

import de.tu_berlin.impro3.scala.spatio_temporal_dynamics._

/**
 * Geographical location represented as (latitude, longitude) coordinates
 * in signed degrees format.
 */
case class Location(lat: Double, lon: Double) {
  require(validGps(gps), "Invalid GPS coordinates")
  lazy val gps  = lat -> lon      // (latitude, longitude) pair
  lazy val mgrs = gps2mgrs(gps)   // Military Grid Reference System
  lazy val zone = mgrs(1e4.toInt) // MGRS zone with 10km precision
  /** Calculate the distance to that Location in km. */
  def <->(that: Location): Double = distanceTo(that)
  /** Calculate the distance to that Location in km. */
  def distanceTo(that: Location): Double = haversine(this.gps, that.gps)
  /** MGRS coordinates truncated to the specified precision. */
  def mgrs(precision: Int): String = truncateMgrs(mgrs, precision)
}

object Location { // companion object
  def apply(gps: (Double, Double)): Location = apply(gps._1, gps._2)
  /** Calculate the geographical midpoint of multiple Locations. */
  def midpoint(locations: Seq[Location]) =
    Location(geo.midpoint(locations.map { _.gps }))
}