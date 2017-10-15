package de.tu_berlin.impro3.flink.spatio_temporal_dynamics.io

import java.io.IOException

import org.apache.flink.api.common.io.OutputFormat
import org.apache.flink.configuration.Configuration
import org.apache.hadoop.hbase._
import org.apache.hadoop.hbase.client._
import org.apache.hadoop.hbase.util.Bytes

class HashTagsOutputFormat[T] extends OutputFormat[T] {

  @transient protected var conn: Connection = _
  @transient protected var table: Table = _

  override def configure(parameters: Configuration): Unit = try {
    val conf = HBaseConfiguration.create()
    conf.set("hbase.rootdir", parameters.getString("hbase", "hdfs://localhost:8020/hbase"))
    conn = ConnectionFactory.createConnection(conf)
    table = conn.getTable(TableName.valueOf("Hashtags"))
  } catch { case ex: Exception =>
    println("Error instantiating a new Hashtags HTable instance", ex)
  }

  override def close(): Unit = {
    if (table != null) table.close()
    table = null
    if (conn != null) conn.close()
    conn = null
  }

  // Write to Hash-tags HBase table.
  override def writeRecord(record: T): Unit = record match {
    case (deg: String, (occ: Int, (zone: String, focus: Double), entropy: Double, spread: Double, (_, _))) =>
      val put = new Put(Bytes.toBytes(deg))
      put.addColumn(Bytes.toBytes("metrics"), Bytes.toBytes("occurrences"), Bytes.toBytes(occ))
      put.addColumn(Bytes.toBytes("metrics"), Bytes.toBytes("focus-zone"), Bytes.toBytes(zone))
      put.addColumn(Bytes.toBytes("metrics"), Bytes.toBytes("focus"), Bytes.toBytes(focus))
      put.addColumn(Bytes.toBytes("metrics"), Bytes.toBytes("entropy"), Bytes.toBytes(entropy))
      put.addColumn(Bytes.toBytes("metrics"), Bytes.toBytes("spread"), Bytes.toBytes(spread))
      table.put(put)

    case (deg: String, (day: Long, (occ: Int, (zone: String, focus: Double), entropy: Double, spread: Double))) =>
      val put = new Put(Bytes.toBytes(deg))
      put.addColumn(Bytes.toBytes("temporal"), Bytes.toBytes(day + ":occurrences"), Bytes.toBytes(occ))
      put.addColumn(Bytes.toBytes("temporal"), Bytes.toBytes(day + ":focus-zone"), Bytes.toBytes(zone))
      put.addColumn(Bytes.toBytes("temporal"), Bytes.toBytes(day + ":focus"), Bytes.toBytes(focus))
      put.addColumn(Bytes.toBytes("temporal"), Bytes.toBytes(day + ":entropy"), Bytes.toBytes(entropy))
      put.addColumn(Bytes.toBytes("temporal"), Bytes.toBytes(day + ":spread"), Bytes.toBytes(spread))
      table.put(put)

    case _ => throw new IOException("Wrong output format!")
  }

  override def open(t: Int, t1: Int): Unit =
    if (table == null) throw new IOException("No HTable provided!")
}
