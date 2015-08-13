package de.tu_berlin.impro3.flink.spatio_temporal_dynamics.metrics

import de.tu_berlin.impro3.flink.spatio_temporal_dynamics._
import geo.Location
import model.HashTag

import org.apache.flink.api.scala._

/** The spread metric. */
object Spread {
  def apply(set: OccurSet): Double = {
    val occurs    = set.values.toStream
    val total     = occurs.map { _.size }.sum
    val locations = occurs.flatMap { _.map { _.location } }
    val midpoint  = Location.midpoint(locations)
    val distance  = locations.map { _ <-> midpoint }.sum
    distance / total
  }

  def apply(cluster: Cluster):        Metric1[Double] =
    cluster.metric(apply)

  def byText(tags: DataSet[HashTag]): Metric1[Double] =
    apply(clusterByText(tags))
}
