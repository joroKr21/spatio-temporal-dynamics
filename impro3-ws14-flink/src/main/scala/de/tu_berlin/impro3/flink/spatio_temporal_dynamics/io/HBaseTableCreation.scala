package de.tu_berlin.impro3.flink.spatio_temporal_dynamics.io

import org.apache.hadoop.hbase._
import org.apache.hadoop.hbase.client._

class HBaseTableCreation {
  def initializeSchema() {
    val conf  = HBaseConfiguration.create()
    val conn  = ConnectionFactory.createConnection(conf)
    val admin = conn.getAdmin

    // Create HTable Hash-tags if it does not exist.
    val hashTags = TableName.valueOf("Hashtags")
    if (!admin.tableExists(hashTags)) {
      val descriptor = new HTableDescriptor(hashTags)
      descriptor.addFamily(new HColumnDescriptor("metrics"))
      descriptor.addFamily(new HColumnDescriptor("temporal"))
      admin.createTable(descriptor)
    }

    // Create HTable Locations if it does not exist.
    val locations = TableName.valueOf("Locations")
    if (!admin.tableExists(locations)){
      val descriptor = new HTableDescriptor(locations)
      descriptor.addFamily(new HColumnDescriptor("metrics"))
      admin.createTable(descriptor)
    }

    // Create HTable SortedLocations if it does not exist.
    val sortedLocations = TableName.valueOf("SortedLocations")
    if (!admin.tableExists(sortedLocations)){
      val descriptor = new HTableDescriptor(sortedLocations)
      descriptor.addFamily(new HColumnDescriptor("location"))
      admin.createTable(descriptor)
    }

    val sortedHashTags = TableName.valueOf("SortedHashtags")
    if (!admin.tableExists(sortedHashTags)){
      val descriptor = new HTableDescriptor(sortedHashTags)
      descriptor.addFamily(new HColumnDescriptor("hashtags"))
      admin.createTable(descriptor)
    }
  }
}
