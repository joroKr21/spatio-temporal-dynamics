package de.tu_berlin.impro3.flink.spatio_temporal_dynamics.io

import java.io.IOException

import org.apache.flink.configuration.Configuration
import org.apache.hadoop.hbase.client.{HBaseAdmin, HTable, Put}
import org.apache.hadoop.hbase.util.Bytes
import org.apache.hadoop.hbase.{HBaseConfiguration, HColumnDescriptor}

class SortedHashtagsOutputFormat[T] extends org.apache.flink.api.common.io.OutputFormat[T] {

  @transient protected var table: HTable = _

  override def configure(parameters: Configuration) {

    val hConf     = HBaseConfiguration.create()
    val admin     = new HBaseAdmin(hConf)
    val tableDesc = new HColumnDescriptor("hashtags")

    hConf.set("hbase.rootdir", parameters.getString("hbase", "hdfs://localhost:8020/hbase"))

    try {
      table =  new HTable(hConf, "SortedHashtags")
    }
    catch {
      case e: Exception => println("Error instantiating a new Sorted-Location HTable instance", e)
    }
  }

  override def close() {
    table.flushCommits()
    table.close()
  }

  //Write to Hashtags HBase table
  override def writeRecord(record: T) = record match{

    case (hashtag: String, (occurrence: Int, (latitude: Double, longitude: Double))) =>

      val put = new Put(Bytes.toBytes(occurrence))

      put.add(Bytes.toBytes("hashtags"), Bytes.toBytes(hashtag +":latitude"), Bytes.toBytes(latitude))
      put.add(Bytes.toBytes("hashtags"), Bytes.toBytes(hashtag +":longitude"), Bytes.toBytes(longitude))

      table.put(put)

    case _ => throw new IOException("Wrong output format!")
  }

  override def open(t: Int, t1: Int) {

    if (table == null) {
      throw new IOException("No HTable provided!")
    }

  }
}
