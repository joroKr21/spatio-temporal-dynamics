package de.tu_berlin.impro3.spark.spatio_temporal_dynamics.metrics

import de.tu_berlin.impro3.spark.spatio_temporal_dynamics._
import model.HashTag

import org.apache.spark.rdd.RDD

/** The adoption lag metric */
object AdoptionLag {
  def apply(set1: OccurSet, set2: OccurSet): Double = {
    val inter = set1.keySet.intersect(set2.keySet)
    val lag   = inter.map { tag =>
      val time1 = set1(tag).minBy { _.time }.time
      val time2 = set2(tag).minBy { _.time }.time
      (time1 - time2).abs
    }.sum.toDouble
    lag / inter.size
  }

  def apply(cluster: Cluster):    Metric2[Double] =
    cluster.measure(apply)

  def byZone(tags: RDD[HashTag]): Metric2[Double] =
    measureByZone(tags) { apply }
}
