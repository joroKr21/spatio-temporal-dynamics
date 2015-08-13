package de.tu_berlin.impro3.scala.spatio_temporal_dynamics.metrics

import de.tu_berlin.impro3.scala.spatio_temporal_dynamics.model.HashTag

import collection.parallel.ParSeq

/** Calculate the total occurrences per HashTag or Location. */
object Occurrences {
  def apply(set: OccurSet): Int =
    set.values.map { _.size }.sum

  def apply(cluster: Cluster):       Metric1[Int] =
    cluster.metric(apply)

  def byText(tags: ParSeq[HashTag]): Metric1[Int] =
    apply(clusterByText(tags))

  def byZone(tags: ParSeq[HashTag]): Metric1[Int] =
    apply(clusterByZone(tags))
}
