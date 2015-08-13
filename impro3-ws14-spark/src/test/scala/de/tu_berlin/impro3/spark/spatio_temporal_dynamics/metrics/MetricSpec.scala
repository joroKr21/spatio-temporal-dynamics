package de.tu_berlin.impro3.spark.spatio_temporal_dynamics.metrics

import de.tu_berlin.impro3.spark.spatio_temporal_dynamics._
import model.HashTag

import org.apache.spark._
import rdd.RDD

import org.junit.runner.RunWith
import org.scalatest._
import junit.JUnitRunner
import prop.PropertyChecks

@RunWith(classOf[JUnitRunner])
class MetricSpec extends PropSpec with PropertyChecks with Matchers {

  implicit val config = PropertyCheckConfig(
    minSuccessful = 1, minSize = 1000,
    maxDiscarded  = 0, maxSize = 1000)

  val name = "Spatio-Temporal Dynamics metric test"
  val conf = new SparkConf().setAppName(name).setMaster("local")
  val sc   = new SparkContext(conf)

  def withDataSet[T](coll: Seq[HashTag])
                    (test: RDD[HashTag] => RDD[T]) = {
    test(sc.parallelize(coll)).collect()
  }

  property("AdoptionLag should always be >= 0") {
    forAll("hashTags") { hashTags: Stream[HashTag] =>
      all { withDataSet(hashTags)
        { AdoptionLag.byZone }.map { _._2 }
      } should be >= 0.0
    }
  }

  property("HashTagSimilarity should always be between 0 and 1") {
    forAll("hashTags") { hashTags: Stream[HashTag] =>
      all { withDataSet(hashTags)
        { HashTagSimilarity.byZone }.map { _._2 }
      } should (be >= 0.0 and be <= 1.0)
    }
  }

  property("Focus should always be between 0 and 1") {
    forAll("hashTags") { hashTags: Stream[HashTag] =>
      all { withDataSet(hashTags)
        { Focus.byText }.map { _._2._2 }
      } should (be > 0.0 and be <= 1.0)
    }
  }

  property("Entropy should always be 0 or >= 1") {
    forAll("hashTags") { hashTags: Stream[HashTag] =>
      all { withDataSet(hashTags)
        { Entropy.byText }.map { _._2 }
      } should (be (0.0) or be >= 1.0)
    }
  }

  property("Spread should always be >= 0") {
    forAll("hashTags") { hashTags: Stream[HashTag] =>
      all { withDataSet(hashTags)
        { Spread.byText }.map { _._2 }
      } should be >= 0.0
    }
  }

  property("SpatialImpact should always be between -1 and 1") {
    forAll("hashTags") { hashTags: Stream[HashTag] =>
      all { withDataSet(hashTags)
        { SpatialImpact.byZone }.map { _._2 }
      } should (be >= -1.0 and be <= 1.0)
    }
  }
}
