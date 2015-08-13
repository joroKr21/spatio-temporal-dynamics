package de.tu_berlin.impro3.flink.spatio_temporal_dynamics.metrics

import de.tu_berlin.impro3.flink.spatio_temporal_dynamics._
import model.HashTag
import geo.Location
import org.junit.runner.RunWith
import org.scalatest._
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class MetricTest extends FunSuite with Matchers {

  test("AdoptionLag between 2 Locations") {
    val hashTags = Seq(
      HashTag("number1",     5e6.toLong, Location(42.7000,  23.3333), None),
      HashTag("second2none", 6e6.toLong, Location(42.7000,  23.3333), None),
      HashTag("number1",     7e6.toLong, Location( 4.5981, -74.0758), None),
      HashTag("second2none", 8e6.toLong, Location( 4.5981, -74.0758), None))

    withDataSet(hashTags) { AdoptionLag.byZone }.toMap should equal {
      Map("18NXL00" -> "34TFN93" -> 2e6.toLong)
    }
  }

  test("HashTagSimilarity between 2 Locations") {
    val hashTags = Seq(
      HashTag("number1",     5e6.toLong, Location(42.7000,  23.3333), None),
      HashTag("second2none", 6e6.toLong, Location(42.7000,  23.3333), None),
      HashTag("second2none", 7e6.toLong, Location( 4.5981, -74.0758), None),
      HashTag("3sacharm",    8e6.toLong, Location( 4.5981, -74.0758), None))

    withDataSet(hashTags) { HashTagSimilarity.byZone }.toMap should equal {
      Map("18NXL00" -> "34TFN93" -> 1/3.0)
    }
  }

  test("Focus of HashTags") {
    val locationA = Location(42.7000,  23.3333)
    val locationB = Location( 4.5981, -74.0758)
    val hashTags  = Seq(
      HashTag("number1",     5e6.toLong, locationA, None),
      HashTag("second2none", 6e6.toLong, locationA, None),
      HashTag("second2none", 7e6.toLong, locationB, None),
      HashTag("second2none", 8e6.toLong, locationB, None))

    withDataSet(hashTags) { Focus.byText }.toMap should equal { Map(
      "number1"     -> ("34TFN93", 42.7000, 23.3333,    1.0),
      "second2none" -> ("18NXL00", 4.5981, -74.0758,  2/3.0))
    }
  }

  test("Entropy of HashTags") {
    val hashTags = Seq(
      HashTag("number1",     5e6.toLong, Location(42.7000,  23.3333), None),
      HashTag("second2none", 6e6.toLong, Location(42.7000,  23.3333), None),
      HashTag("second2none", 7e6.toLong, Location( 4.5981, -74.0758), None))

    withDataSet(hashTags) { Entropy.byText }.toMap should equal {
      Map("number1" -> 0.0, "second2none" -> 1.0)
    }
  }

  test("Spread of HashTags") {
    val distance = geo.haversine(42.7000 -> 23.3333, 4.5981 -> -74.0758) / 2
    val hashTags = Seq(
      HashTag("number1",     5e6.toLong, Location(42.7000,  23.3333), None),
      HashTag("second2none", 6e6.toLong, Location(42.7000,  23.3333), None),
      HashTag("second2none", 7e6.toLong, Location( 4.5981, -74.0758), None))

    withDataSet(hashTags) { Spread.byText }.foreach {
      case ("number1",     spread) => spread should equal (0.0)
      case ("second2none", spread) => spread should equal (distance +- 1e-6)
      case (_,             spread) => spread should be >= 0.0
    }
  }

  test("SpatialImpact between 2 Locations") {
    val hashTags = Seq(
      HashTag("number1",     5e6.toLong, Location(42.7000,  23.3333), None),
      HashTag("second2none", 6e6.toLong, Location(42.7000,  23.3333), None),
      HashTag("second2none", 7e6.toLong, Location( 4.5981, -74.0758), None),
      HashTag("3sacharm",    8e6.toLong, Location( 4.5981, -74.0758), None))

    withDataSet(hashTags) { SpatialImpact.byZone }.toMap should equal {
      Map("18NXL00" -> "34TFN93" -> -1/3.0)
    }
  }

  test("Occurrences by HashTag and by Location") {
    val hashTags = Seq(
      HashTag("number1",     5e6.toLong, Location(42.7000,  23.3333), None),
      HashTag("second2none", 6e6.toLong, Location(42.7000,  23.3333), None),
      HashTag("second2none", 7e6.toLong, Location( 4.5981, -74.0758), None),
      HashTag("3sacharm",    8e6.toLong, Location( 4.5981, -74.0758), None))

    withDataSet(hashTags) { Occurrences.byText }.toMap should equal {
      Map("number1" -> 1, "second2none" -> 2, "3sacharm" -> 1)
    }

    withDataSet(hashTags) { Occurrences.byZone }.toMap should equal {
      Map("18NXL00" -> 2, "34TFN93" -> 2)
    }
  }

  test("Lifespan of HashTags") {
    val hashTags = Seq(
      HashTag("number1",     5e6.toLong, Location(42.7000,  23.3333), None),
      HashTag("second2none", 6e6.toLong, Location(42.7000,  23.3333), None),
      HashTag("second2none", 7e6.toLong, Location( 4.5981, -74.0758), None),
      HashTag("number1",     8e6.toLong, Location( 4.5981, -74.0758), None))

    withDataSet(hashTags) { Lifespan.byText }.toMap should equal { Map(
      "number1"     -> (5e6.toLong, 8e6.toLong),
      "second2none" -> (6e6.toLong, 7e6.toLong))
    }
  }

  test("Midpoint of HashTags") {
    val la = Location(42.7000,  23.3333)
    val lb = Location( 4.5981, -74.0758)
    val midpoint = Location.midpoint(Seq(la, lb)).gps
    val hashTags = Seq(
      HashTag("number1",     5e6.toLong, la, None),
      HashTag("second2none", 6e6.toLong, la, None),
      HashTag("second2none", 7e6.toLong, lb, None),
      HashTag("number1",     8e6.toLong, lb, None))

    withDataSet(hashTags) { Midpoint.byText }.toMap should equal { Map(
      "number1"     -> midpoint,
      "second2none" -> midpoint)
    }
  }
}
