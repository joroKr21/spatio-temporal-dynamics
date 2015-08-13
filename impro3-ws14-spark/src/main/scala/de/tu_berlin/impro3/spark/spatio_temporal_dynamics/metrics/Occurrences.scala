package de.tu_berlin.impro3.spark.spatio_temporal_dynamics.metrics

import de.tu_berlin.impro3.spark.spatio_temporal_dynamics.model.HashTag
import org.apache.spark.rdd.RDD

/** Calculate the total occurrences per HashTag or Location. */
object Occurrences {
  def apply(set: OccurSet): Int =
    set.values.map { _.size }.sum

  def apply(cluster: Cluster):        Metric1[Int] =
    cluster.measure(apply)

  def byText(tags: RDD[HashTag]): Metric1[Int] =
    measureByText(tags) { apply }

  def byZone(tags: RDD[HashTag]): Metric1[Int] =
    measure1(clusterByZone(tags)) { apply }
}
