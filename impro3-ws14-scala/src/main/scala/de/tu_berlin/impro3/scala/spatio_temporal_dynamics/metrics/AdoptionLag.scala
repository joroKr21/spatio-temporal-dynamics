package de.tu_berlin.impro3.scala.spatio_temporal_dynamics.metrics

import de.tu_berlin.impro3.scala.spatio_temporal_dynamics._
import model.HashTag

import collection.parallel.ParSeq

/** The adoption lag metric. */
object AdoptionLag {
  def apply(set1: OccurSet, set2: OccurSet): Double = {
    val inter = set1.keySet.intersect(set2.keySet)
    val lag   = inter.toSeq.map { tag =>
      val time1 = set1(tag).minBy { _.time }.time
      val time2 = set2(tag).minBy { _.time }.time
      (time1 - time2).abs
    }.sum.toDouble
    lag / inter.size
  }

  def apply(cluster: Cluster):      Metric2[Double] =
    cluster.metric(apply)

  def apply(tags: ParSeq[HashTag]): Metric2[Double] =
    apply(clusterByZone(tags))
}
