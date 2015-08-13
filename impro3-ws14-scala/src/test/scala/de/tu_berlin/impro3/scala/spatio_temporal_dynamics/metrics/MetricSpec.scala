package de.tu_berlin.impro3.scala.spatio_temporal_dynamics.metrics

import de.tu_berlin.impro3.scala.spatio_temporal_dynamics._
import model.HashTag

import org.junit.runner.RunWith
import org.scalatest._
import junit.JUnitRunner
import prop.PropertyChecks

@RunWith(classOf[JUnitRunner])
class MetricSpec extends PropSpec with PropertyChecks with Matchers {

  implicit val config = PropertyCheckConfig(
    maxDiscarded = 0, minSize = 100, maxSize = 200, workers = 8)

  property("AdoptionLag should always be >= 0") {
    forAll { hashTags: Seq[HashTag] =>
      all(AdoptionLag(hashTags.par).values) should be >= 0.0
    }
  }

  property("HashTagSimilarity should always be between 0 and 1") {
    forAll { hashTags: Seq[HashTag] =>
      all(HashTagSimilarity(hashTags.par).values) should
        (be >= 0.0 and be <= 1.0)
    }
  }

  property("Focus should always be between 0 and 1") {
    forAll { hashTags: Seq[HashTag] =>
      all(Focus(hashTags.par).values.map { _._2 }) should
        (be > 0.0 and be <= 1.0)
    }
  }

  property("Entropy should always be 0 or >= 1") {
    forAll { hashTags: Seq[HashTag] =>
      all(Entropy(hashTags.par).values) should
        (be (0.0) or be >= 1.0)
    }
  }

  property("Spread should always be >= 0") {
    forAll { hashTags: Seq[HashTag] =>
      all(Spread(hashTags.par).values) should be >= 0.0
    }
  }

  property("SpatialImpact should always be between -1 and 1") {
    forAll { hashTags: Seq[HashTag] =>
      all(SpatialImpact(hashTags.par).values) should
        (be >= -1.0 and be <= 1.0)
    }
  }

  property("Occurrences should always be > 0 and correct") {
    forAll { hashTags: Seq[HashTag] =>
      lazy val byText = Occurrences.byText(hashTags.par)
      lazy val byZone = Occurrences.byZone(hashTags.par)
      all(byText.values) should be > 0
      all(byZone.values) should be > 0
      byText should equal (hashTags.groupBy { _.text }.mapValues { _.size })
      byZone should equal (hashTags.groupBy { _.zone }.mapValues { _.size })
    }
  }

  property("Lifespan should always be correct") {
    forAll { hashTags: Seq[HashTag] =>
      Lifespan(hashTags.par).values.foreach {
        case (first, last) => first should (be > 0l and be <= last)
      }
    }
  }
}
