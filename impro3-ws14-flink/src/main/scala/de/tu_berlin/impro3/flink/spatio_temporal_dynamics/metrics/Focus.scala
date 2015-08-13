package de.tu_berlin.impro3.flink.spatio_temporal_dynamics.metrics

import de.tu_berlin.impro3.flink.spatio_temporal_dynamics._
import geo.Location
import model.HashTag

import org.apache.flink.api.scala._

/** The focus metric. */
object Focus {
  def apply(set: OccurSet): (String, Double, Double, Double) = {
    val sizes = set.mapValues { _.size }
    val total = sizes.values.sum.toDouble
    val (zone, max) = sizes.maxBy { _._2 }
    val (lat,  lon) = Location.midpoint(set(zone).map { _.location }).gps
    (zone, lat, lon, max / total)
  }

  def apply(cluster: Cluster):        Metric1[(String, Double, Double, Double)] =
    cluster.metric(apply)

  def byText(tags: DataSet[HashTag]): Metric1[(String, Double, Double, Double)] =
    apply(clusterByText(tags))
}
