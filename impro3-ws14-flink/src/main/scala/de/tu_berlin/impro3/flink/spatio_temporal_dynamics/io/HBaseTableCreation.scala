package de.tu_berlin.impro3.flink.spatio_temporal_dynamics.io

import org.apache.hadoop.hbase.HBaseConfiguration._
import org.apache.hadoop.hbase.client.HBaseAdmin
import org.apache.hadoop.hbase.{HColumnDescriptor, HTableDescriptor}

class HBaseTableCreation {

    def initializeSchema() {

      val conf  = create()
      val admin = new HBaseAdmin(conf)

      //Create HTable Hashtags if it does not exit
      if ( !  admin.tableExists("Hashtags") ){
        val descHashtags      = new HTableDescriptor("Hashtags")
        val metricsHashtags   = new HColumnDescriptor("metrics")
        val temporalHashtags  = new HColumnDescriptor("temporal")

        descHashtags.addFamily(metricsHashtags)
        descHashtags.addFamily(temporalHashtags)

        admin.createTable(descHashtags)
      }


      //Create HTable Locations if it does not exit
      if ( !  admin.tableExists("Locations")){
        val descLocation      = new HTableDescriptor("Locations")
        val metricsLocation   = new HColumnDescriptor("metrics")

        descLocation.addFamily(metricsLocation)

        admin.createTable(descLocation)
      }

      //Create HTable Locations if it does not exit
      if ( ! admin.tableExists("SortedLocations")){
        val descSorted      = new HTableDescriptor("SortedLocations")
        val metricsSorted   = new HColumnDescriptor("location")

        descSorted.addFamily(metricsSorted)

        admin.createTable(descSorted)
      }


      if ( ! admin.tableExists("SortedHashtags")){
        val descSortedHashtags      = new HTableDescriptor("SortedHashtags")
        val metricsSortedHashtags   = new HColumnDescriptor("hashtags")

        descSortedHashtags.addFamily(metricsSortedHashtags)

        admin.createTable(descSortedHashtags)
      }

    }
}
