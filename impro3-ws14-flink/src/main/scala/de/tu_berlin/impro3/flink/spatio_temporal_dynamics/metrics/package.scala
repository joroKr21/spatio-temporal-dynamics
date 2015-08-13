package de.tu_berlin.impro3.flink.spatio_temporal_dynamics

import model.HashTag

import org.apache.flink.api.scala._
import org.apache.flink.api.common.typeinfo.TypeInformation

/** Package object with some common functions and constants. */
package object metrics {
  // set of HashTag occurrences
  type OccurSet   = Map[String, Seq[HashTag]]
  // cluster of OccurSets (grouped by a specific key)
  type Cluster    = DataSet[(String, OccurSet)]
  // the result of unary metric function
  type Metric1[T] = DataSet[(String, T)]
  // the result of binary metric function
  type Metric2[T] = DataSet[((String, String), T)]

  /**
   * Cluster the [[HashTag]]s by text and generate their occurrence sets.
   * @param hashTags [[DataSet]] of [[HashTag]]s
   * @return [[Cluster]] of [[HashTag]] occurrence sets grouped by text
   */
  def clusterByText(hashTags: DataSet[HashTag]): Cluster =
    hashTags.groupBy { _.text }.reduceGroup { tags =>
      val seq = tags.toStream
      seq.head.text -> seq.groupBy { _.zone }
    }

  /**
   * Cluster the [[HashTag]]s by zone and generate their occurrence sets.
   * @param hashTags [[DataSet]] of [[HashTag]]s
   * @return [[Cluster]] of [[HashTag]] occurrence sets grouped by zone
   */
  def clusterByZone(hashTags: DataSet[HashTag]): Cluster =
    hashTags.groupBy { _.zone }.reduceGroup { tags =>
      val seq = tags.toStream
      seq.head.zone -> seq.groupBy { _.text }
    }

  /**
   * Cluster the [[HashTag]]s by city and generate their occurrence sets.
   * @param hashTags [[DataSet]] of [[HashTag]]s
   * @return [[Cluster]] of [[HashTag]] occurrence sets grouped by city
   */
  def clusterByCity(hashTags: DataSet[HashTag]): Cluster = hashTags
    .filter  { _.city.isDefined }
    .groupBy { _.city.get }
    .reduceGroup { tags =>
      val seq = tags.toStream
      seq.head.city.get -> seq.groupBy { _.text }
    }

  /** Syntactic sugar. */
  implicit class ClusterOps(cluster: Cluster) {
    /** This is a cool cross join optimization. */
    def conjoin(other: Cluster) = other.flatMap {
      _._2.keys.toVector.sorted.combinations(2).map { xs => xs(0) -> xs(1) }
    }.distinct.join(cluster).where { _._1 }.equalTo { _._1 }.map { tuple =>
      tuple._1 -> tuple._2._2
    }.join(cluster).where { _._1._2 }.equalTo { _._1 }.map { tuple =>
      val ((keys, left), (_, right)) = tuple
      keys -> (left, right)
    }
    
    def filterTotalSize(minSize: Int) =
      cluster.filter { _._2.values.map { _.size }.sum >= minSize }
    
    def filterSetSize  (minSize: Int) =
      cluster.map { _.mapVal { _.filter { _._2.size >= minSize } } }

    /** Apply a unary metric function to the [[Cluster]]. */
    def metric[T](f: OccurSet => T)
                 (implicit t: TypeInformation[(String, T)]) =
      cluster.map { _.mapVal(f) }

    /** Apply a symmetric binary function to the conjoined [[Cluster]]s. */
    def symmetric[T](other: Cluster)(f: (OccurSet, OccurSet) => T)
                    (implicit t: TypeInformation[((String, String), T)]) =
      conjoin(other).map { _.mapVal(f.tupled) }

    /** Partition the [[Cluster]] by time intervals and apply the metric f. */
    def temporal[T](interval: Long)(metric: OccurSet => T)
                   (implicit t: TypeInformation[(String, (Long, T))]) =
      cluster.flatMap { tuple =>
        tuple._2.map { case (key, set) =>
          set.map { key -> _ }
        }.flatten.groupBy {
          _._2.time / interval
        }.mapValues { tags =>
          val set = tags.groupBy { _._1 }
            .mapValues { _.toStream.map { _._2 } }
          metric(set)
        }.toStream.map { tuple._1 -> _ }
      }
  }

  /** Define the cartesian product of 2 sequences. */
  implicit class Cartesian[A](xs: Seq[A]) {
    def cross[B](ys: Seq[B]) = for (x <- xs; y <- ys) yield x -> y
    def x    [B](ys: Seq[B]) = cross(ys)
  }

  /** Some syntactic sugar for [[Pair]]s. */
  implicit class MapValue[K, V](tuple: (K, V)) {
    def mapVal[R](f: V => R) = tuple._1 -> f(tuple._2)
  }
}
