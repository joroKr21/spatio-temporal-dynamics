package de.tu_berlin.impro3.scala.spatio_temporal_dynamics

import model.HashTag

import collection.parallel.{ ParSeq, ParMap }

/** Package object with some common functions and constants. */
package object metrics {
  /** A set of HashTag occurrences. */
  type OccurSet   = Map[String, Seq[HashTag]]
  /** A cluster of [[OccurSet]]s (grouped by a specific key). */
  type Cluster    = ParMap[String, OccurSet]
  /** The result of an unary metric. */
  type Metric1[T] = ParMap[String, T]
  /** The result of a binary metric. */
  type Metric2[T] = ParMap[(String, String), T]
  /** The result of a temporal metric. */
  type MetricT[T] = ParMap[(String, Long), T]

  /**
   * Cluster the [[HashTag]]s by text and generate their occurrence sets.
   * @param tags sequence of [[HashTag]]s
   * @return [[Cluster]] of [[HashTag]] occurrence sets grouped by text
   */
  def clusterByText(tags: ParSeq[HashTag]): Cluster =
    tags.groupBy { _.text }.mapValues { _.seq.groupBy { _.zone } }

  /**
   * Cluster the [[HashTag]]s by zone and generate their occurrence sets.
   * @param tags sequence of [[HashTag]]s
   * @return [[Cluster]] of [[HashTag]] occurrence sets grouped by zone
   */
  def clusterByZone(tags: ParSeq[HashTag]): Cluster =
    tags.groupBy { _.zone }.mapValues { _.seq.groupBy { _.text } }

  /** Syntax sugar. */
  implicit class ClusterOps(cluster: Cluster) {
    /** All second order combinations of the [[Cluster]]. */
    lazy val combinations = for {
      (key1, set1) <- cluster
      (key2, set2) <- cluster
      if key1 < key2
      if set1.keys.exists(set2.contains)
    } yield (key1, key2) -> (set1, set2)

    /** Remove [[OccurSet]]s with smaller size from the [[Cluster]]. */
    def filterTotalSize(min: Int) = cluster.filter {
      _._2.values.map { _.size }.sum >= min
    }

    /** Remove elements with smaller size from all [[OccurSet]]s. */
    def filterSetSize  (min: Int) = cluster.map {
      case (key, set) => key -> set.filter { _._2.size >= min }
    }

    /** Apply an unary metric function to the [[Cluster]]. */
    def metric[T](f: OccurSet => T):             Metric1[T] =
      cluster.mapValues(f)

    /** Apply a binary metric function to the [[Cluster]]. */
    def metric[T](f: (OccurSet, OccurSet) => T): Metric2[T] =
      combinations.mapValues(f.tupled)

    /** Partition the [[Cluster]] by time intervals and apply the metric f. */
    def temporal[T](interval: Long)(f: OccurSet => T): MetricT[T] =
      cluster.flatMap { case (key, set) =>
        set.flatMap { case (group, tags) =>
          tags.map { group -> _ }
        }.groupBy {
          _._2.time / interval
        }.mapValues { tags =>
          f(tags.groupBy { _._1 }.mapValues { _.toSeq.map { _._2 } })
        }.map { case (time, metric) => key -> time -> metric }
      }
  }

  /** Define the cartesian product of 2 sequences. */
  implicit class Cartesian[A](xs: Seq[A]) {
    def cross[B](ys: Seq[B]) = for (x <- xs; y <- ys) yield x -> y
    def x    [B](ys: Seq[B]) = cross(ys)
  }
}
