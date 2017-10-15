package de.tu_berlin.impro3.flink.spatio_temporal_dynamics.io

import java.io.IOException

import org.apache.flink.api.common.io.OutputFormat
import org.apache.flink.configuration.Configuration
import org.apache.hadoop.hbase._
import org.apache.hadoop.hbase.client._
import org.apache.hadoop.hbase.util.Bytes

class SortedHashtagsOutputFormat[T] extends OutputFormat[T] {

  @transient protected var conn: Connection = _
  @transient protected var table: Table = _

  override def configure(parameters: Configuration): Unit = try {
    val conf = HBaseConfiguration.create()
    conf.set("hbase.rootdir", parameters.getString("hbase", "hdfs://localhost:8020/hbase"))
    conn = ConnectionFactory.createConnection(conf)
    table = conn.getTable(TableName.valueOf("SortedHashtags"))
  } catch { case ex: Exception =>
    println("Error instantiating a new Sorted-Location HTable instance", ex)
  }

  override def close(): Unit = {
    if (table != null) table.close()
    table = null
    if (conn != null) conn.close()
    conn = null
  }

  // Write to Hash-tags HBase table.
  override def writeRecord(record: T): Unit = record match {
    case (tag: String, (occ: Int, (lat: Double, lng: Double))) =>
      val put = new Put(Bytes.toBytes(occ))
      put.addColumn(Bytes.toBytes("hashtags"), Bytes.toBytes(tag +":latitude"), Bytes.toBytes(lat))
      put.addColumn(Bytes.toBytes("hashtags"), Bytes.toBytes(tag +":longitude"), Bytes.toBytes(lng))
      table.put(put)

    case _ => throw new IOException("Wrong output format!")
  }

  override def open(t: Int, t1: Int): Unit =
    if (table == null) throw new IOException("No HTable provided!")
}
