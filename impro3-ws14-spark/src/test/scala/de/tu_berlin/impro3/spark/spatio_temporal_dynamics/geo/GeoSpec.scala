package de.tu_berlin.impro3.spark.spatio_temporal_dynamics.geo

import de.tu_berlin.impro3.spark.spatio_temporal_dynamics._

import math._

import org.junit.runner.RunWith
import org.scalacheck.Gen
import org.scalatest._
import junit.JUnitRunner
import matchers.{ BeMatcher, MatchResult }
import prop.PropertyChecks

@RunWith(classOf[JUnitRunner])
class GeoSpec extends PropSpec with PropertyChecks with Matchers {

  property("The haversin function should always be between 0 and 1") {
    forAll { fi: Double =>
      haversin(fi) should (be >= 0.0 and be <= 1.0)
    }
  }

  property("The haversine distance between 2 points should always be >= 0") {
    forAll(Gen.zip(gpsGen, gpsGen)) { case (gps1, gps2) =>
      haversine(gps1, gps2) should be >= 0.0
    }
  }

  property("The geographical midpoint should always have valid coordinates") {
    forAll(Gen.listOf(gpsGen)) { coordinates =>
      midpoint(coordinates) should be (ValidGps)
    }
  }

  property("All MGRS coordinates should follow the same pattern") {
    forAll(mgrsGen) { gps =>
      gps2mgrs(gps) should fullyMatch regex """[0-6]\d[A-Z]{3} \d{5} \d{5}"""
    }
  }

  /**
   * We use [[http://www.movable-type.co.uk/scripts/latlong.html]]
   * to calculate the geographical distance for the tests.
   */
  val distances = Table(
    ("lat1",  "lon1",   "lat2",  "lon2",  "distance"),
    (41.9033,  12.4533, 41.9000,  12.5000,     3.882), // Vatican  <-> Rome
    (42.7000,  23.3333, 52.5167,  13.3833,  1319.000), // Sofia    <-> Berlin
    (42.3482, -75.1890, 35.6895, 139.6917, 10640.000)  // New York <-> Tokyo
  )

  property("The haversine formula should calculate the distance between 2" +
    " cities") {
    forAll(distances) { (lat1, lon1, lat2, lon2, distance) =>
      val acc = pow(10, ceil(log10(distance)) - 4) / 2
      haversine(lat1 -> lon1, lat2 -> lon2) should equal (distance +- acc)
    }
  }

  /**
   * We use [[http://www.earthpoint.us/Convert.aspx]]
   * to calculate the MGRS coordinates for the tests.
   */
  val mgrsCoordinates = Table(
    ( "lat",   "lon",    "mgrs"),
    ( 42.7000,  23.3333, "34TFN 91110 30140"), // Sofia
    (  4.5981, -74.0758, "18NXL 02515 08305"), // Bogota
    ( 55.7500,  37.6167, "37UDB 13173 79122"), // Moscow
    (-33.8600, 151.2094, "56HLH 34360 51924")  // Sydney
  )

  property("The Geo-location converter should convert (latitude, longitude)" +
    " coordinates to MGRS coordinates with 10m precision") {
    forAll(mgrsCoordinates) { (lat, lon, mgrs) =>
      gps2mgrs(lat -> lon, 10) should be (truncateMgrs(mgrs, 10))
    }
  }

  /**
   * We use [[http://www.geomidpoint.com]]
   * to calculate the geographical midpoints for the tests.
   */
  val midpoints = Table(("coordinates", "midpoint"),
    (Nil,
       0.0000 ->   0.0000), // 0
    (41.9033 -> 12.4533 :: Nil,
      41.9033 ->  12.4533), // 1
    (41.9000 -> 12.5000 :: 42.7000 ->  23.3333 :: Nil,
      42.4276 ->  17.8821), // 2
    (52.5167 -> 13.3833 :: 42.3482 -> -75.1890 :: 35.6895 -> 139.6917 :: Nil,
      85.2988 -> -16.6264)  // 3
  )

  property("The Geo-midpoint calculator should calculate the geographical" +
    " midpoint of multiple locations.") {
    forAll(midpoints) { case (coordinates, (lat, lon)) =>
      val (x, y) = midpoint(coordinates)
      x should equal (lat +- 1e-4)
      y should equal (lon +- 1e-4)
    }
  }

  object ValidGps extends BeMatcher[(Double, Double)] {
    def apply(gps: (Double, Double)) = MatchResult(
      validGps(gps),
      s"$gps were valid GPS coordinates",
      s"$gps were invalid GPS coordinates"
    )
  }
}
