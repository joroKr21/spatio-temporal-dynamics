package de.tu_berlin.impro3.flink.spatio_temporal_dynamics.io

import java.io.IOException

import org.apache.flink.configuration.Configuration
import org.apache.hadoop.hbase._
import org.apache.hadoop.hbase.client.{HBaseAdmin, HTable, Put}
import org.apache.hadoop.hbase.util.Bytes


class LocationsOutputFormat[T] extends org.apache.flink.api.common.io.OutputFormat[T] {

  @transient protected var table: HTable = _

  override def configure(parameters: Configuration) {

    val hConf     = HBaseConfiguration.create()
    val admin     = new HBaseAdmin(hConf)
    val tableDesc = new HColumnDescriptor("metrics")

    hConf.set("hbase.rootdir", parameters.getString("hbase", "hdfs://localhost:8020/hbase"))

    try {
      table =  new HTable(hConf, "Locations")
    }
    catch {
      case e: Exception => println("Error instantiating a new Location HTable instance", e)
    }
  }

  override def close() {
    table.flushCommits()
    table.close()
  }

  //Write to Hashtags HBase table
  override def writeRecord(record: T) = record match{

    case ((zoneA: String, zoneB:String), (similarity:Double, adoption_lag:Long, spatial_impact:Double)) =>

      val putA = new Put(Bytes.toBytes(zoneA))

      putA.add(Bytes.toBytes("metrics"), Bytes.toBytes(zoneB + ":spatial_impact"), Bytes.toBytes(spatial_impact))
      putA.add(Bytes.toBytes("metrics"), Bytes.toBytes(zoneB + ":similarity"), Bytes.toBytes(similarity))
      putA.add(Bytes.toBytes("metrics"), Bytes.toBytes(zoneB + ":adoption_lag"), Bytes.toBytes(adoption_lag))

      table.put(putA)

      val putB = new Put(Bytes.toBytes(zoneB))

      putB.add(Bytes.toBytes("metrics"), Bytes.toBytes(zoneA + ":spatial_impact"), Bytes.toBytes(-spatial_impact))
      putB.add(Bytes.toBytes("metrics"), Bytes.toBytes(zoneA + ":similarity"), Bytes.toBytes(similarity))
      putB.add(Bytes.toBytes("metrics"), Bytes.toBytes(zoneA + ":adoption_lag"), Bytes.toBytes(adoption_lag))

      table.put(putB)

    case _ => throw new IOException("Wrong output format!")
  }

  override def open(t: Int, t1: Int) {

    if (table == null) {
      throw new IOException("No HTable provided!")
    }

  }
}
