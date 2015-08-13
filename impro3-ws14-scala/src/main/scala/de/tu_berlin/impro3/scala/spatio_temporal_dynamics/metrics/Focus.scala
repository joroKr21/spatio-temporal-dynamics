package de.tu_berlin.impro3.scala.spatio_temporal_dynamics.metrics

import de.tu_berlin.impro3.scala.spatio_temporal_dynamics._
import model.HashTag

import collection.parallel.ParSeq

/** The focus metric. */
object Focus {
  def apply(set: OccurSet): (String, Double) = {
    val sizes = set.mapValues { _.size }
    val total = sizes.values.sum.toDouble
    val (zone, max) = sizes.maxBy { _._2 }
    zone -> max / total
  }

  def apply(cluster: Cluster):      Metric1[(String, Double)] =
    cluster.metric(apply)

  def apply(tags: ParSeq[HashTag]): Metric1[(String, Double)] =
    apply(clusterByText(tags))
}
