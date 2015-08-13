package de.tu_berlin.impro3.scala.spatio_temporal_dynamics.metrics

import de.tu_berlin.impro3.scala.spatio_temporal_dynamics._
import model.HashTag

import collection.parallel.ParSeq

/** The HashTag similarity metric. */
object HashTagSimilarity {
  def apply(set1: OccurSet, set2: OccurSet): Double = {
    val inter = set1.keys.count(set2.contains)
    val union = set1.size + set2.size - inter
    inter / union.toDouble
  }

  def apply(cluster: Cluster):      Metric2[Double] =
    cluster.metric(apply)

  def apply(tags: ParSeq[HashTag]): Metric2[Double] =
    apply(clusterByZone(tags))
}
