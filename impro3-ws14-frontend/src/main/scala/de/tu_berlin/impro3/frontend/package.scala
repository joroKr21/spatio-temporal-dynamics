package de.tu_berlin.impro3

import org.apache.hadoop.hbase.HBaseConfiguration
import org.apache.hadoop.hbase.client.{HTable, HBaseAdmin}
import org.apache.hadoop.hbase.util.Bytes

package object frontend {
  // Byte conversion helper methods
  val _i = (x : Array[Byte]) => Bytes.toInt(x)
  val _l = (x : Array[Byte]) => Bytes.toLong(x)
  val _d = (x : Array[Byte]) => Bytes.toDouble(x)
  val _s = (x : Array[Byte]) => Bytes.toString(x)
  val _b = (x : Any) => Bytes.toBytes(x.toString)

  // Hbase
  val conf = new HBaseConfiguration()
  val admin = new HBaseAdmin(conf)
  val tableLocations = new HTable(conf, "Locations")
  val tableLocationsSorted = new HTable(conf, "SortedLocations")
  val tableHashtags = new HTable(conf, "Hashtags")
  val tableHashtagsSorted = new HTable(conf, "SortedHashtags")
  val tableHashtagDist = new HTable(conf, "HashtagDistribution")
}

