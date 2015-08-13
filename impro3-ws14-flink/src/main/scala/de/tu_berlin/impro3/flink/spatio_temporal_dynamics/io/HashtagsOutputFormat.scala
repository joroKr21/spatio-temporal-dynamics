package de.tu_berlin.impro3.flink.spatio_temporal_dynamics.io

import java.io.IOException

import org.apache.flink.configuration.Configuration
import org.apache.hadoop.hbase._
import org.apache.hadoop.hbase.client.{HBaseAdmin, HTable, Put}
import org.apache.hadoop.hbase.util.Bytes


class HashtagsOutputFormat[T] extends org.apache.flink.api.common.io.OutputFormat[T] {

  @transient protected var table: HTable = _

  override def configure(parameters: Configuration) {

    val hConf     = HBaseConfiguration.create()
    val admin     = new HBaseAdmin(hConf)
    val tableDesc = new HColumnDescriptor("metrics")

    hConf.set("hbase.rootdir", parameters.getString("hbase", "hdfs://localhost:8020/hbase"))

    try {
      table =  new HTable(hConf, "Hashtags")
    }
    catch {
      case e: Exception => println("Error instantiating a new Hashtags HTable instance", e)
    }
  }

  override def close() {
    table.flushCommits()
    table.close()
  }

  //Write to Hashtags HBase table
  override def writeRecord(record: T) = record match{

    case (deg:String, (occ:Int, (zone:String, focus:Double), entropy:Double, spread:Double, (_, _))) =>

      val put = new Put(Bytes.toBytes(deg))

      put.add(Bytes.toBytes("metrics"), Bytes.toBytes("occurrences"), Bytes.toBytes(occ))
      put.add(Bytes.toBytes("metrics"), Bytes.toBytes("focus-zone"), Bytes.toBytes(zone))
      put.add(Bytes.toBytes("metrics"), Bytes.toBytes("focus"), Bytes.toBytes(focus))
      put.add(Bytes.toBytes("metrics"), Bytes.toBytes("entropy"), Bytes.toBytes(entropy))
      put.add(Bytes.toBytes("metrics"), Bytes.toBytes("spread"), Bytes.toBytes(spread))

      table.put(put)

    case (deg:String, (day:Long, (occ:Int, (zone:String, focus:Double), entropy:Double, spread:Double))) =>

      val put = new Put(Bytes.toBytes(deg))

      put.add(Bytes.toBytes("temporal"), Bytes.toBytes(day + ":occurrences"), Bytes.toBytes(occ))
      put.add(Bytes.toBytes("temporal"), Bytes.toBytes(day + ":focus-zone"), Bytes.toBytes(zone))
      put.add(Bytes.toBytes("temporal"), Bytes.toBytes(day + ":focus"), Bytes.toBytes(focus))
      put.add(Bytes.toBytes("temporal"), Bytes.toBytes(day + ":entropy"), Bytes.toBytes(entropy))
      put.add(Bytes.toBytes("temporal"), Bytes.toBytes(day + ":spread"), Bytes.toBytes(spread))

      table.put(put)

    case _ => throw new IOException("Wrong output format!")
  }

  override def open(t: Int, t1: Int) {

    if (table == null) {
      throw new IOException("No HTable provided!")
    }
  }
}
