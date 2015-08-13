package de.tu_berlin.impro3.flink.spatio_temporal_dynamics

import de.jotschi.geoconvert.GeoConvert

import math._

/** Package object with some common geographical functions and constants. */
package object geo {
  val R = 6371.009 // mean Earth radius in km according to IUGG

  /**
   * The haversin function.
   *
   * @param fi angle in radians
   * @return haversin(fi) in radians
   */
  def haversin(fi: Double) = {
    // NOTE: Do not replace with cos to avoid floating point errors.
    val sinFi = sin(fi / 2)
    sinFi * sinFi
  }

  /**
   * Calculates the distance between 2 geographical points expressed in
   * (latitude, longitude) coordinates.
   *
   * See [[http://en.wikipedia.org/wiki/Haversine_formula Haversine formula]].
   *
   * @param gps1 (latitude, longitude) in degrees of the first point
   * @param gps2 (latitude, longitude) in degrees of the second point
   * @return distance in km between the 2 geographical points
   */
  def haversine(gps1: (Double, Double), gps2: (Double, Double)) = {
    require(validGps(gps1) && validGps(gps2), "Invalid GPS coordinates")
    val (lat1, lon1) = gps1
    val (lat2, lon2) = gps2
    val dLat = (lat1 - lat2).toRadians
    val dLon = (lon1 - lon2).toRadians
    val c    = cos(lat1.toRadians) * cos(lat2.toRadians)
    2 * R * asin(sqrt(haversin(dLat) + c * haversin(dLon)))
  }

  /**
   * Calculate the geographical midpoint of multiple locations expressed in
   * (latitude, longitude) coordinates.
   *
   * See [[http://www.geomidpoint.com/calculation.html Calculation Methods]].
   *
   * @param coordinates sequence of (latitude, longitude) coordinates
   * @return the geographical midpoint of all locations
   */
  def midpoint(coordinates: Seq[(Double, Double)]) =
    coordinates.map { gps =>
      require(validGps(gps), "Sequence contains invalid GPS coordinates")
      val (lat, lon) = gps._1.toRadians -> gps._2.toRadians
      (cos(lat) * cos(lon), cos(lat) * sin(lon), sin(lat))
    }.fold(0.0, 0.0, 0.0) {
      case ((x1, y1, z1), (x2, y2, z2)) => (x1 + x2, y1 + y2, z1 + z2)
    } match { case (x, y, z) =>
      atan2(z, sqrt(x * x + y * y)).toDegrees -> atan2(y, x).toDegrees
    }

  /**
   * Convert a (latitude, longitude) [[Pair]] to MGRS (Military Grid Reference
   * System) coordinates.
   *
   * See [[http://en.wikipedia.org/wiki/Military_grid_reference_system
   * Military grid reference system]].
   *
   * @param gps (latitude, longitude) coordinates in degrees
   * @return MGRS coordinates of the geographical point as a [[String]]
   */
  def gps2mgrs(gps: (Double, Double)): String = {
    require(validMgrs(gps), "Cannot convert to MGRS coordinates")
    val mgrs = GeoConvert.toMGRS(gps._2, gps._1).replaceFirst(" ", "")
    if (mgrs.length == 16) 0 + mgrs else mgrs
  }


  /**
   * Convert a (latitude, longitude) [[Pair]] to MGRS (Military Grid Reference
   * System) coordinates with a specified precision.
   *
   * See [[http://en.wikipedia.org/wiki/Military_grid_reference_system
   * Military grid reference system]].
   *
   * @param gps (latitude, longitude) coordinates in degrees
   * @param precision precision in meters (from 1m to 100km)
   * @return MGRS coordinates of the geographical point as a [[String]]
   */
  def gps2mgrs(gps: (Double, Double), precision: Int): String =
    truncateMgrs(gps2mgrs(gps), precision)

  /**
   * Check if the specified (latitude, longitude) coordinates are in the valid
   * signed degrees format range.
   *
   * Latitude:   -90 to  90 degrees.
   * Longitude: -180 to 180 degrees.
   *
   * @param gps (latitude, longitude) coordinates in signed degrees format
   * @return true if in valid range
   */
  def validGps(gps: (Double, Double)) = gps match { case (lat, lon) =>
    (lat >= -90 && lat <= 90) && (lon >= -180 && lon <= 180)
  }

  /**
   * Check if the specified (latitude, longitude) coordinates are convertible
   * to MGRS coordinates.
   *
   * Latitude:   -80 to  84 degrees.
   * Longitude: -180 to 180 degrees.
   *
   * @param gps (latitude, longitude) coordinates in signed degrees format
   * @return true if in valid range
   */
  def validMgrs(gps: (Double, Double)) = validGps(gps) &&
    (gps match { case (lat, _) => lat >= -80 && lat < 84 })

  /**
   * Truncate MGRS coordinates to a specified precision.
   *
   * @param mgrs MGRS coordinates as a [[String]]
   * @param precision precision in meters (from 1m to 100km)
   * @return MGRS coordinates truncated to the specified precision
   */
  def truncateMgrs(mgrs: String, precision: Int) = {
    // precision from 1m to 100km
    require(precision > 0 && precision <= 1e5, "Invalid precision")
    val coordinates = mgrs.split(' ').toList
    val accuracy    = log10(precision).toInt
    (coordinates.head ::
      coordinates.tail
        .map { _.dropRight(accuracy) }
        .filter { _.nonEmpty }).mkString(" ")
  }
}
