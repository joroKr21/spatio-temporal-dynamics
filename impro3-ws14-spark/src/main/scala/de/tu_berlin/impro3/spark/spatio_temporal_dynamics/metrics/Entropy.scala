package de.tu_berlin.impro3.spark.spatio_temporal_dynamics.metrics

import de.tu_berlin.impro3.spark.spatio_temporal_dynamics._
import model.HashTag

import org.apache.spark.rdd.RDD

import math.log

/** The entropy metric */
object Entropy {
  val lnOf2 = log(2)
  def apply(set: OccurSet): Double = {
    val sizes = set.values.map { _.size }
    val total = sizes.sum.toDouble
    sizes.map { n =>
      val prob = n / total
      prob * log(prob) / lnOf2
    }.sum.abs
  }

  def apply(cluster: Cluster):    Metric1[Double] =
    cluster.measure(apply)

  def byText(tags: RDD[HashTag]): Metric1[Double] =
    measureByText(tags) { apply }
}
