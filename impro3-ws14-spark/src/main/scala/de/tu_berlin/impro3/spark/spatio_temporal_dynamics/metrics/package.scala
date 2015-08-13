package de.tu_berlin.impro3.spark.spatio_temporal_dynamics

import model.HashTag

import org.apache.spark.rdd.RDD

/** Package object with some common functions and constants. */
package object metrics {
  // set of HashTag occurrences
  type OccurSet   = Map[String, Seq[HashTag]]
  // cluster of OccurSets (grouped by a specific key)
  type Cluster    = RDD[(String, OccurSet)]
  // the result of unary metric function
  type Metric1[T] = RDD[(String, T)]
  // the result of binary metric function
  type Metric2[T] = RDD[((String, String), T)]

  /**
   * Cluster the [[HashTag]]s by text and generate their occurrence sets.
   * @param hashTags [[RDD]] of [[HashTag]]s
   * @return [[Cluster]] of [[HashTag]] occurrence sets grouped by text
   */
  def clusterByText(hashTags: RDD[HashTag]): Cluster =
    hashTags.groupBy { _.text }.map {
      _.mapRight { _.toStream.groupBy { _.zone } }
    }

  /**
   * Cluster the [[HashTag]]s by zone and generate their occurrence sets.
   * @param hashTags [[RDD]] of [[HashTag]]s
   * @return [[Cluster]] of [[HashTag]] occurrence sets grouped by zone
   */
  def clusterByZone(hashTags: RDD[HashTag]): Cluster =
    hashTags.groupBy { _.zone }.map {
      _.mapRight { _.toStream.groupBy { _.text } }
    }

  /**
   * Apply unary metric function to the [[Cluster]].
   * @param cluster [[Cluster]] of [[HashTag]] occurrences.
   * @param metric unary metric function
   * @return the result from the metric function applied to the [[Cluster]]
   */
  def measure1[T](cluster: Cluster)
                 (metric: OccurSet => T) =
    cluster.map { _.mapRight(metric) }

  /**
   * Apply binary metric function to all combinations from the [[Cluster]].
   * @param cluster [[Cluster]] of [[HashTag]] occurrences.
   * @param metric symmetric binary metric function
   * @return the result from the metric function applied to the [[Cluster]]
   */
  def measure2[T](cluster: Cluster)
                 (metric: (OccurSet, OccurSet) => T) =
    combinations(cluster).map { _.mapRight(metric.tupled) }

  def measureByText[T](hashTags: RDD[HashTag])
                      (metric: OccurSet => T) =
    measure1(clusterByText(hashTags)) { metric }

  def measureByZone[T](hashTags: RDD[HashTag])
                      (metric: (OccurSet, OccurSet) => T) =
    measure2(clusterByZone(hashTags)) { metric }

  /** Syntactic sugar. */
  implicit class ClusterOps(cluster: Cluster) {
    def combinations = metrics.combinations(cluster)
    def filterTotalSize(minSize: Int) =
      cluster.filter { _._2.values.map { _.size }.sum >= minSize }
    def filterSetSize  (minSize: Int) =
      cluster.map { _.mapRight { _.filter { _._2.size >= minSize } } }
    def measure[T](metric: OccurSet => T): Metric1[T] =
      measure1(cluster) { metric }
    def measure[T](metric: (OccurSet, OccurSet) => T): Metric2[T] =
      measure2(cluster) { metric }
  }
  
  /** Build all non-empty combinations of size 2 from the [[Cluster]]. */
  def combinations(cluster: Cluster) =
    cluster.cartesian(cluster).collect {
      case ((key1, set1), (key2, set2))
        if key1 < key2 && set1.keys.exists(set2.contains) =>
        (key1, key2) -> (set1, set2)
  }

  /** Define the cartesian product of 2 sequences. */
  implicit class Cartesian[A](xs: Seq[A]) {
    def cross[B](ys: Seq[B]) = for (x <- xs; y <- ys) yield x -> y
    def x    [B](ys: Seq[B]) = cross(ys)
  }

  /** Some syntactic sugar for [[Pair]]s. */
  implicit class MapRight[A, B](tuple: (A, B)) {
    def mapRight[C](f: B => C) = tuple._1 -> f(tuple._2)
  }
}
