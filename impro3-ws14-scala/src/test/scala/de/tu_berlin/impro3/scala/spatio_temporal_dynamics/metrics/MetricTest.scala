package de.tu_berlin.impro3.scala.spatio_temporal_dynamics.metrics

import de.tu_berlin.impro3.scala.spatio_temporal_dynamics._
import model.HashTag
import geo.Location
import org.junit.runner.RunWith
import org.scalatest._
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class MetricTest extends FunSuite with Matchers {

  test("AdoptionLag between 2 Locations") {
    val hashTags = Seq(
      HashTag("number1",     5e6.toLong, Location(42.7000,  23.3333)),
      HashTag("second2none", 6e6.toLong, Location(42.7000,  23.3333)),
      HashTag("number1",     7e6.toLong, Location( 4.5981, -74.0758)),
      HashTag("second2none", 8e6.toLong, Location( 4.5981, -74.0758))).par

    AdoptionLag(hashTags) should equal {
      Map("18NXL 0 0" -> "34TFN 9 3" -> 2e6.toLong)
    }
  }

  test("HashTagSimilarity between 2 Locations") {
    val hashTags = Seq(
      HashTag("number1",     5e6.toLong, Location(42.7000,  23.3333)),
      HashTag("second2none", 6e6.toLong, Location(42.7000,  23.3333)),
      HashTag("second2none", 7e6.toLong, Location( 4.5981, -74.0758)),
      HashTag("3sacharm",    8e6.toLong, Location( 4.5981, -74.0758))).par

    HashTagSimilarity(hashTags) should equal {
      Map("18NXL 0 0" -> "34TFN 9 3" -> 1/3.0)
    }
  }

  test("Focus of HashTags") {
    val hashTags = Seq(
      HashTag("number1",     5e6.toLong, Location(42.7000,  23.3333)),
      HashTag("second2none", 6e6.toLong, Location(42.7000,  23.3333)),
      HashTag("second2none", 7e6.toLong, Location( 4.5981, -74.0758)),
      HashTag("second2none", 8e6.toLong, Location( 4.5981, -74.0758))).par

    Focus(hashTags) should equal { Map(
      "number1"     -> ("34TFN 9 3",   1.0),
      "second2none" -> ("18NXL 0 0", 2/3.0))
    }
  }

  test("Entropy of HashTags") {
    val hashTags = Seq(
      HashTag("number1",     5e6.toLong, Location(42.7000,  23.3333)),
      HashTag("second2none", 6e6.toLong, Location(42.7000,  23.3333)),
      HashTag("second2none", 7e6.toLong, Location( 4.5981, -74.0758))).par

    Entropy(hashTags) should equal {
      Map("number1" -> 0.0, "second2none" -> 1.0)
    }
  }

  test("Spread of HashTags") {
    val distance = geo.haversine(42.7000 -> 23.3333, 4.5981 -> -74.0758) / 2
    val hashTags = Seq(
      HashTag("number1",     5e6.toLong, Location(42.7000,  23.3333)),
      HashTag("second2none", 6e6.toLong, Location(42.7000,  23.3333)),
      HashTag("second2none", 7e6.toLong, Location( 4.5981, -74.0758))).par

    Spread(hashTags).foreach {
      case ("number1",     spread) => spread should equal (0.0)
      case ("second2none", spread) => spread should equal (distance +- 1e-6)
      case (_,             spread) => spread should be >= 0.0
    }
  }

  test("SpatialImpact between 2 Locations") {
    val hashTags = Seq(
      HashTag("number1",     5e6.toLong, Location(42.7000,  23.3333)),
      HashTag("second2none", 6e6.toLong, Location(42.7000,  23.3333)),
      HashTag("second2none", 7e6.toLong, Location( 4.5981, -74.0758)),
      HashTag("3sacharm",    8e6.toLong, Location( 4.5981, -74.0758))).par

    SpatialImpact(hashTags) should equal {
      Map("18NXL 0 0" -> "34TFN 9 3" -> -1/3.0)
    }
  }

  test("Occurrences by HashTag and by Location") {
    val hashTags = Seq(
      HashTag("number1",     5e6.toLong, Location(42.7000,  23.3333)),
      HashTag("second2none", 6e6.toLong, Location(42.7000,  23.3333)),
      HashTag("second2none", 7e6.toLong, Location( 4.5981, -74.0758)),
      HashTag("3sacharm",    8e6.toLong, Location( 4.5981, -74.0758))).par

    Occurrences.byText(hashTags) should equal {
      Map("number1" -> 1, "second2none" -> 2, "3sacharm" -> 1)
    }

    Occurrences.byZone(hashTags) should equal {
      Map("18NXL 0 0" -> 2, "34TFN 9 3" -> 2)
    }
  }

  test("Lifespan of HashTags") {
    val hashTags = Seq(
      HashTag("number1",     5e6.toLong, Location(42.7000,  23.3333)),
      HashTag("second2none", 6e6.toLong, Location(42.7000,  23.3333)),
      HashTag("second2none", 7e6.toLong, Location( 4.5981, -74.0758)),
      HashTag("number1",     8e6.toLong, Location( 4.5981, -74.0758))).par

    Lifespan(hashTags) should equal { Map(
      "number1"     -> (5e6.toLong, 8e6.toLong),
      "second2none" -> (6e6.toLong, 7e6.toLong))
    }
  }
}
