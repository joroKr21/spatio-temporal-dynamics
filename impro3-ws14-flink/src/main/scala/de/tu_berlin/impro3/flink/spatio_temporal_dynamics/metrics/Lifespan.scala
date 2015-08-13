package de.tu_berlin.impro3.flink.spatio_temporal_dynamics.metrics

import de.tu_berlin.impro3.flink.spatio_temporal_dynamics._
import model.HashTag
import org.apache.flink.api.scala._

/** First and last occurrence of a HashTag. */
object Lifespan {
  def apply(set: OccurSet): (Long, Long) = {
    val times = set.values.flatten.map { _.time }
    times.min -> times.max
  }

  def apply(cluster: Cluster):        Metric1[(Long, Long)] =
    cluster.metric(apply)

  def byText(tags: DataSet[HashTag]): Metric1[(Long, Long)] =
    apply(clusterByText(tags))
}
