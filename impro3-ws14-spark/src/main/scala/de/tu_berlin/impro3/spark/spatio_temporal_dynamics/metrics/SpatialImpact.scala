package de.tu_berlin.impro3.spark.spatio_temporal_dynamics.metrics

import de.tu_berlin.impro3.spark.spatio_temporal_dynamics._
import model.HashTag

import org.apache.spark.rdd.RDD

/** The spatial impact metric. */
object SpatialImpact {
  def apply(set1: OccurSet, set2: OccurSet): Double = {
    val union = set1.keySet.union(set2.keySet)
    union.map { impact(_, set1, set2) }.sum / union.size
  }

  def apply(cluster: Cluster):    Metric2[Double] =
    cluster.measure { apply }

  def byZone(tags: RDD[HashTag]): Metric2[Double] =
    measureByZone(tags) { apply }

  def preceding [T: Ordering](cartesian: Seq[(T, T)]) =
    cartesian.count { case (x, y) => Ordering[T].lt(x, y) }

  def succeeding[T: Ordering](cartesian: Seq[(T, T)]) =
    preceding(cartesian) { Ordering[T].reverse }

  // impact based on specified HashTag
  def impact(tag: String, set1: OccurSet, set2: OccurSet) =
    if (set1.contains(tag) && set2.contains(tag)) {
      val cartesian = set1(tag) x set2(tag)
      val pre = preceding (cartesian)
      val suc = succeeding(cartesian)
      (pre - suc) / cartesian.size.toDouble
    } else if (set1.contains(tag)) 1.0 else -1.0
}
