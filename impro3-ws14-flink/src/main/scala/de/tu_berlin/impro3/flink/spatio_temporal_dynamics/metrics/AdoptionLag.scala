package de.tu_berlin.impro3.flink.spatio_temporal_dynamics.metrics

import de.tu_berlin.impro3.flink.spatio_temporal_dynamics._
import model.HashTag

import org.apache.flink.api.scala._

/** The adoption lag metric */
object AdoptionLag {
  def apply(set1: OccurSet, set2: OccurSet): Long = {
    val inter = set1.keySet.intersect(set2.keySet)
    val lag   = inter.toSeq.map { tag =>
      val time1 = set1(tag).map { _.time }.min
      val time2 = set2(tag).map { _.time }.max
      (time1 - time2).abs
    }.sum
    lag / inter.size
  }

  def apply(cluster1: Cluster, cluster2: Cluster): Metric2[Long] =
    cluster1.symmetric(cluster2) { apply }

  def byZone(tags: DataSet[HashTag]):              Metric2[Long] =
    apply(clusterByZone(tags), clusterByText(tags))
}
