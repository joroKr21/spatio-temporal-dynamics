package de.tu_berlin.impro3.flink.spatio_temporal_dynamics.io

import org.apache.flink.api.common.io.OutputFormat
import org.apache.flink.configuration.Configuration
import org.apache.hadoop.hbase._
import client._
import util.Bytes

import language.{ implicitConversions, reflectiveCalls }

import java.math.BigDecimal
import java.nio.ByteBuffer

abstract class HBaseOutputFormat[T](tableNames: String*)
                                   (implicit config: Map[String, String])
  extends OutputFormat[T] {

  var tables = Map.empty[String, HTable]

  def configure(parameters: Configuration) = ()

  def open(taskNumber: Int, numTasks: Int) = {
    val hConf = HBaseConfiguration.create
    for { (key, value) <- config if key.startsWith("hbase") }
      hConf.set(key, value)
    for { name <- tableNames } tables += name -> new HTable(hConf, name)
  }

  def close() = try withResources(tables.values.toSeq: _*) {
    _.foreach { _.flushCommits() }
  } finally tables = Map.empty

  def withResource [R <: { def close(): Unit }](resource:  R )
                                               (f: R      => Unit) =
    try f(resource) finally resource.close()

  def withResources[R <: { def close(): Unit }](resources: R*)
                                               (f: Seq[R] => Unit) =
    resources.foldRight(f) { (r, f) =>
      _ => withResource(r) { _ => f(resources) }
    } (resources)
}

object HBaseOutputFormat {
  def apply[T](tableNames: String*)
              (write: (Map[String, HTable], T) => Unit)
              (implicit config: Map[String, String]) =
    new HBaseOutputFormat[T](tableNames: _*) {
      def writeRecord(record: T) = write(tables, record)
    }

  implicit def toBytes(x: Boolean   ): Array[Byte] = Bytes.toBytes(x)
  implicit def toBytes(x: Byte      ): Array[Byte] = Bytes.toBytes(x)
  implicit def toBytes(x: Char      ): Array[Byte] = Bytes.toBytes(x)
  implicit def toBytes(x: Short     ): Array[Byte] = Bytes.toBytes(x)
  implicit def toBytes(x: Int       ): Array[Byte] = Bytes.toBytes(x)
  implicit def toBytes(x: Long      ): Array[Byte] = Bytes.toBytes(x)
  implicit def toBytes(x: Float     ): Array[Byte] = Bytes.toBytes(x)
  implicit def toBytes(x: Double    ): Array[Byte] = Bytes.toBytes(x)
  implicit def toBytes(x: BigDecimal): Array[Byte] = Bytes.toBytes(x)
  implicit def toBytes(x: String    ): Array[Byte] = Bytes.toBytes(x)
  implicit def toBytes(x: ByteBuffer): Array[Byte] = Bytes.toBytes(x)

  implicit def toByteArrays(x: String       ): Array[Array[Byte]] =
    Bytes.toByteArrays(x)
  implicit def toByteArrays(x: Array[Byte]  ): Array[Array[Byte]] =
    Bytes.toByteArrays(x)
  implicit def toByteArrays(x: Array[String]): Array[Array[Byte]] =
    Bytes.toByteArrays(x)
}
