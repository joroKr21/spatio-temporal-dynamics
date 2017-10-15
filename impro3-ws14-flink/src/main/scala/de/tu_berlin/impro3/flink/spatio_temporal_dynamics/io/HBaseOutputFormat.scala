package de.tu_berlin.impro3.flink.spatio_temporal_dynamics.io

import org.apache.flink.api.common.io.OutputFormat
import org.apache.flink.configuration.Configuration
import org.apache.hadoop.hbase._
import client._
import util.Bytes

import language.{ implicitConversions, reflectiveCalls }

import java.math.BigDecimal
import java.nio.ByteBuffer

abstract class HBaseOutputFormat[T](tableNames: String*)(
  implicit config: Map[String, String]
) extends OutputFormat[T] {

  var conn: Connection = _
  var tables = Map.empty[String, Table]

  def configure(parameters: Configuration): Unit = ()

  def open(taskNumber: Int, numTasks: Int): Unit = {
    val conf = HBaseConfiguration.create
    for ((k, v) <- config) if (k.startsWith("hbase")) conf.set(k, v)
    conn = ConnectionFactory.createConnection(conf)
    for (name <- tableNames) tables += name -> conn.getTable(TableName.valueOf(name))
  }

  def close(): Unit = {
    tables.values.foreach(_.close())
    tables = Map.empty
    if (conn != null) conn.close()
    conn = null
  }
}

object HBaseOutputFormat {
  def apply[T](tableNames: String*)(write: (Map[String, Table], T) => Unit)(
    implicit config: Map[String, String]
  ): HBaseOutputFormat[T] = new HBaseOutputFormat[T](tableNames: _*) {
    def writeRecord(record: T): Unit = write(tables, record)
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
