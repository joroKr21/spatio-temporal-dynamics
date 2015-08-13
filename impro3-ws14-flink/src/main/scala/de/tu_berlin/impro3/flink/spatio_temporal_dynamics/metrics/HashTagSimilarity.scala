package de.tu_berlin.impro3.flink.spatio_temporal_dynamics.metrics

import de.tu_berlin.impro3.flink.spatio_temporal_dynamics._
import model.HashTag

import org.apache.flink.api.scala._

/** The HashTag similarity metric. */
object HashTagSimilarity {
  def apply(set1: OccurSet, set2: OccurSet): Double = {
    val inter = set1.keys.count(set2.contains)
    val union = set1.size + set2.size - inter
    inter / union.toDouble
  }

  def apply(cluster1: Cluster, cluster2: Cluster): Metric2[Double] =
    cluster1.symmetric(cluster2) { apply }

  def byZone(tags: DataSet[HashTag]):              Metric2[Double] =
    apply(clusterByZone(tags), clusterByText(tags))
}
