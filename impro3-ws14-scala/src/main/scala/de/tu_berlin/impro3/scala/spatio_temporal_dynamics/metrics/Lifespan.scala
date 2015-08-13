package de.tu_berlin.impro3.scala.spatio_temporal_dynamics.metrics

import de.tu_berlin.impro3.scala.spatio_temporal_dynamics.model.HashTag

import scala.collection.parallel.ParSeq

/** Calculate the lifespan of a HashTag. */
object Lifespan {
  def apply(set: OccurSet): (Long, Long) = {
    val times = set.flatMap { _._2.map { _.time } }
    times.min -> times.max
  }

  def apply(cluster: Cluster):      Metric1[(Long, Long)] =
    cluster.metric(apply)

  def apply(tags: ParSeq[HashTag]): Metric1[(Long, Long)] =
    apply(clusterByText(tags))
}
