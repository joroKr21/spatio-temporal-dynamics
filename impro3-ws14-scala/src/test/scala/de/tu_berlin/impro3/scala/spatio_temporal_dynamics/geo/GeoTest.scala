package de.tu_berlin.impro3.scala.spatio_temporal_dynamics.geo

import org.junit.runner.RunWith
import org.scalatest._
import junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class GeoTest extends FlatSpec with Matchers {

  "Invalid GPS coordinates" should "cause an IllegalArgumentException" in {
    val loLat = (-90.0001,    0.0000)
    val hiLat = ( 90.0001,    0.0000)
    val loLon = (  0.0000, -180.0001)
    val hiLon = (  0.0000,  180.0001)
    Seq(loLat, hiLat, loLon, hiLon).foreach { gps =>
      val ex1 = intercept[IllegalArgumentException] { Location(gps) }
      ex1.getMessage.toLowerCase should include ("invalid gps coordinates")
      val ex2 = intercept[IllegalArgumentException] { haversine(gps, gps) }
      ex2.getMessage.toLowerCase should include ("invalid gps coordinates")
      val ex3 = intercept[IllegalArgumentException] { midpoint(gps :: Nil) }
      ex3.getMessage.toLowerCase should include ("invalid gps coordinates")
      val ex4 = intercept[IllegalArgumentException] { gps2mgrs(gps) }
      ex4.getMessage.toLowerCase should include ("cannot convert to mgrs")
    }
  }

  "Some GPS coordinates" should "not be convertible to MGRS coordinates" in {
    val loLat = (-80.0001, 0.0000)
    val hiLat = ( 84.0001, 0.0000)
    Seq(loLat, hiLat).foreach { gps =>
      val ex = intercept[IllegalArgumentException] { gps2mgrs(gps) }
      ex.getMessage.toLowerCase should include ("cannot convert to mgrs")
    }
  }
}
