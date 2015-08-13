package de.tu_berlin.impro3.flink.spatio_temporal_dynamics.metrics

import de.tu_berlin.impro3.flink.spatio_temporal_dynamics.model.HashTag
import org.apache.flink.api.scala._

/** Calculate the total occurrences per HashTag or Location. */
object Occurrences {
  def apply(set: OccurSet): Int =
    set.values.map { _.size }.sum

  def apply(cluster: Cluster):        Metric1[Int] =
    cluster.metric(apply)

  def distribution(set: OccurSet) = set.mapValues { _.size }

  def byText(tags: DataSet[HashTag]): Metric1[Int] =
    apply(clusterByText(tags))

  def byZone(tags: DataSet[HashTag]): Metric1[Int] =
    apply(clusterByZone(tags))
}
