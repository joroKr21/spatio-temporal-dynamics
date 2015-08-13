package de.tu_berlin.impro3.spark.spatio_temporal_dynamics.metrics

import de.tu_berlin.impro3.spark.spatio_temporal_dynamics._
import model.HashTag

import org.apache.spark.rdd.RDD

/** The focus metric. */
object Focus {
  def apply(set: OccurSet): (String, Double) = {
    val sizes = set.mapValues { _.size }
    val total = sizes.values.sum.toDouble
    val (zone, max) = sizes.maxBy { _._2 }
    zone -> max / total
  }

  def apply(cluster: Cluster):    Metric1[(String, Double)] =
    cluster.measure(apply)

  def byText(tags: RDD[HashTag]): Metric1[(String, Double)] =
    measureByText(tags) { apply }
}
