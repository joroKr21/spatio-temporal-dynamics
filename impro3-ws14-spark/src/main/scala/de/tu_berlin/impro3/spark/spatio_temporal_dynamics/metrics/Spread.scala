package de.tu_berlin.impro3.spark.spatio_temporal_dynamics.metrics

import de.tu_berlin.impro3.spark.spatio_temporal_dynamics._
import geo.Location
import model.HashTag

import org.apache.spark.rdd.RDD

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

  def apply(cluster: Cluster):    Metric1[Double] =
    cluster.measure(apply)

  def byText(tags: RDD[HashTag]): Metric1[Double] =
    measureByText(tags) { apply }
}
