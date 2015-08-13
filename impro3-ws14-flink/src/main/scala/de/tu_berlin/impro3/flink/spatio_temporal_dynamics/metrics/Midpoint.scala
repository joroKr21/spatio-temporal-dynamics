package de.tu_berlin.impro3.flink.spatio_temporal_dynamics.metrics

import de.tu_berlin.impro3.flink.spatio_temporal_dynamics.geo.Location
import de.tu_berlin.impro3.flink.spatio_temporal_dynamics.model.HashTag
import org.apache.flink.api.scala._

/** Calculate the midpoint of all occurrences per HashTag or Location. */
object Midpoint {
  def apply(set: OccurSet): (Double, Double) =
    Location.midpoint(set.values.flatten.map { _.location }.toSeq).gps

  def apply(cluster: Cluster):        Metric1[(Double, Double)] =
    cluster.metric(apply)

  def distribution(set: OccurSet) = set.mapValues { tags =>
    Location.midpoint(tags.map { _.location }).gps
  }

  def byText(tags: DataSet[HashTag]): Metric1[(Double, Double)] =
    apply(clusterByText(tags))

  def byZone(tags: DataSet[HashTag]): Metric1[(Double, Double)] =
    apply(clusterByZone(tags))
}
