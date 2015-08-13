package de.tu_berlin.impro3.frontend

import de.tu_berlin.impro3.frontend.Models._
import org.apache.hadoop.hbase.client.{Get, Result, Scan}
import org.apache.hadoop.hbase.filter.PageFilter
import org.json4s.{DefaultFormats, Formats}
import org.scalatra.ScalatraServlet
import org.scalatra.json._

import scala.collection.JavaConversions._


class LocationController extends ScalatraServlet with JacksonJsonSupport {
  // Sets up automatic case class to JSON output serialization, required by
  // the JValueResult trait.
  protected implicit val jsonFormats: Formats = DefaultFormats

  // Before every action runs, set the content type to be in JSON format.
  before() {
    contentType = formats("json")
  }

  get("/top") {
    Locations.top(2000)
  }

  get("/details/:region") {
    Locations.details(params("region").toString)
  }

}

object Locations {
  def top(amount : Int) = {
    val scan = new Scan
    scan.setReversed(true)
    scan.setFilter(new PageFilter(amount))
    val scanner = tableLocationsSorted.getScanner(scan)
    val scalaList: List[Result] = scanner.iterator.toList

    val locations = scalaList.map{ row =>
      val locations = row.getFamilyMap(_b("location")).keySet().map { x =>
        val column = _s(x)
        column.split(":")(0)
      }

      locations.map{ loc =>
        val lat = _d(row.getValue(_b("location"), _b(loc + ":latitude")))
        val lon = _d(row.getValue(_b("location"), _b(loc + ":longitude")))
        Map("lat" -> lat.toString, "lon" -> lon.toString, "loc" -> loc)
      }
    }

    val occurrences = scalaList.map(x => _i(x.getRow).toString).toList
    val map = occurrences.zip(locations).toMap

    occurrences.map{ occ =>
      val set = map(occ)
      set.map{ map =>
        MapLocation(occ.toInt, map("lat").toDouble, map("lon").toDouble, map("loc"))
      }
    }.flatten.take(amount)

  }

  private def spatial_impact(result : Result) = {
    val cityMap = Map("10SEG58" -> "San Fransisco",
      "11SLT76" -> "Farmington",
      "11SLT75" -> "Manhattan",
      "16SGC43" -> "Atlanta",
      "18TWL80" -> "New York")

    val map = {
      for {
        (name, value) <- result.getFamilyMap(_b("metrics"))
        if _s(name).endsWith(":spatial_impact")
      } yield {
        val location = _s(name).split(":")(0)
        (location, _d(value))
      }
    }.toMap

    val scale = 0.01
    val getDistGroup = (value : Double) => (value / scale).toLong
    val distMap = map.values.groupBy(x => getDistGroup(x))

    val distribution  = {
      for (a <- -100 until 100) yield {
        if (distMap.contains(a))
          distMap(a).size
        else 0
      }
    }

    // We need the plus 100 because we want range [-1.0, -.99] to be group 0
    val cities = cityMap.keys.filter(x => map.contains(x)).map(x => cityMap(x) -> (getDistGroup(map(x)) + 100)).toMap

    SpatialImpact(distribution, cities)
  }

  private def entropy_focus_spread(result : Result) = {
    val map = result.getFamilyMap(_b("hashtags"))
    val occurrences = map.map{case (key, value) => (_s(key), _i(value))}.toMap

    occurrences.keySet.map(hashtag => Hashtags.metrics(Hashtags.get(hashtag), hashtag))
  }.toList

  def details(region : String) = {
    val get = new Get(_b(region))
    get.addFamily(_b("metrics"))
    get.addFamily(_b("hashtags"))
    get.setMaxVersions(1)
    val result = tableLocations.get(get)

    LocationDetails(spatial_impact(result), entropy_focus_spread(result))
  }
}
