package de.tu_berlin.impro3.flink.spatio_temporal_dynamics.io

import java.io.IOException

import org.apache.flink.api.common.io.OutputFormat
import org.apache.flink.configuration.Configuration
import org.apache.hadoop.hbase._
import org.apache.hadoop.hbase.client._
import org.apache.hadoop.hbase.util.Bytes

class LocationsOutputFormat[T] extends OutputFormat[T] {

  @transient protected var conn: Connection = _
  @transient protected var table: Table = _

  override def configure(parameters: Configuration): Unit = try {
    val conf = HBaseConfiguration.create()
    conf.set("hbase.rootdir", parameters.getString("hbase", "hdfs://localhost:8020/hbase"))
    conn = ConnectionFactory.createConnection(conf)
    table = conn.getTable(TableName.valueOf("Locations"))
  } catch { case ex: Exception =>
    println("Error instantiating a new Location HTable instance", ex)
  }

  override def close(): Unit = {
    if (table != null) table.close()
    table = null
    if (conn != null) conn.close()
    conn = null
  }

  // Write to Hash-tags HBase table.
  override def writeRecord(record: T): Unit = record match {
    case ((za: String, zb: String), (sim: Double, adoption: Long, spatial: Double)) =>
      val putA = new Put(Bytes.toBytes(za))
      putA.addColumn(Bytes.toBytes("metrics"), Bytes.toBytes(zb + ":spatial_impact"), Bytes.toBytes(spatial))
      putA.addColumn(Bytes.toBytes("metrics"), Bytes.toBytes(zb + ":similarity"), Bytes.toBytes(sim))
      putA.addColumn(Bytes.toBytes("metrics"), Bytes.toBytes(zb + ":adoption_lag"), Bytes.toBytes(adoption))
      table.put(putA)

      val putB = new Put(Bytes.toBytes(zb))
      putB.addColumn(Bytes.toBytes("metrics"), Bytes.toBytes(za + ":spatial_impact"), Bytes.toBytes(-spatial))
      putB.addColumn(Bytes.toBytes("metrics"), Bytes.toBytes(za + ":similarity"), Bytes.toBytes(sim))
      putB.addColumn(Bytes.toBytes("metrics"), Bytes.toBytes(za + ":adoption_lag"), Bytes.toBytes(adoption))
      table.put(putB)

    case _ => throw new IOException("Wrong output format!")
  }

  override def open(t: Int, t1: Int): Unit =
    if (table == null) throw new IOException("No HTable provided!")
}
