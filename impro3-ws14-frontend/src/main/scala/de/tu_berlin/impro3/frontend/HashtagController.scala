package de.tu_berlin.impro3.frontend

import de.tu_berlin.impro3.frontend.Models._
import org.apache.hadoop.hbase.HBaseConfiguration
import org.apache.hadoop.hbase.client._
import org.apache.hadoop.hbase.filter.PageFilter
import org.json4s.{DefaultFormats, Formats}
import org.scalatra.ScalatraServlet
import org.scalatra.json._

import _root_.scala.collection.JavaConversions._


class HashtagController extends ScalatraServlet with JacksonJsonSupport {
  // Sets up automatic case class to JSON output serialization, required by
  // the JValueResult trait.
  protected implicit val jsonFormats: Formats = DefaultFormats

  // Before every action runs, set the content type to be in JSON format.
  before() {
    contentType = formats("json")
  }

  get("/") {
    "no endpoint"
  }

  get("/top/:amount") {
    Hashtags.top(params("amount").toInt)
  }

  get("/:hashtag") {
    Hashtags.one(params("hashtag").toString)
  }

}

object Hashtags {
  def metrics(result : Result, hashtag: String) : HashtagMetrics = {
    val metricsMap = result.getFamilyMap(_b("metrics"))

    val getMetric = (x : String) => metricsMap(_b(x))

    HashtagMetrics(
      hashtag,
      _i(getMetric("occurrences")),
      _s(getMetric("focus-zone")),
      _d(getMetric("focus")),
      _d(getMetric("entropy")),
      _d(getMetric("spread"))
    )
  }

  def temporal(result : Result) : List[TemporalItem] = {
    val temporalMap = result.getFamilyMap(_b("temporal"))
    val dayMetric = (x : String) => temporalMap(_b(x))
    val days = {
      for {
        (name, _) <- temporalMap
        if _s(name).endsWith(":occurrences")
      } yield {
        _s(name).split(":")(0)
      }
    }.toList
    val dayMetrics = (x : String) =>
      TemporalItem(x.toLong,
        _i(dayMetric(x+":occurrences")),
        //_s(dayMetric(x+":focus-zone")),
        "test",
        _d(dayMetric(x+":focus")),
        _d(dayMetric(x+":entropy")),
        _d(dayMetric(x+":spread"))
      )

    days.map(day => dayMetrics(day.toString))
  }.toList

  def locations(result: Result, hashtag : String) : List[HashtagLocation] = {
    val getDist = new Get(_b(hashtag))
    getDist.addFamily(_b("locations"))
    getDist.setMaxVersions(1)
    val resDist = tableHashtagDist.get(getDist)
    val distMap = resDist.getFamilyMap(_b("locations"))
    val distribution = {
      for {
        (name, value) <- distMap
      } yield {
        val loc = _s(name).split(":")(0)
        (loc, _i(value))
      }
    }.toMap

    distribution.keySet.map{ loc =>
      val lat = _d(distMap(_b(loc + ":latitude")))
      val lon = _d(distMap(_b(loc + ":longitude")))
      HashtagLocation(distribution(loc), lat, lon)
    }
  }.toList


  def one(hashtag: String) = {
    val result = get(hashtag)
    val hashtagMetrics = metrics(result, hashtag)
    val hashtagTemporal = temporal(result)
    val hashtagLocations = locations(result, hashtag)

    Hashtag(hashtag, hashtagMetrics, hashtagTemporal, hashtagLocations)
  }

  def top(amount: Int) = {
    val scan = new Scan
    scan.setReversed(true)
    scan.setFilter(new PageFilter(amount))
    val scanner = tableHashtagsSorted.getScanner(scan)
    val scalaList: List[Result] = scanner.iterator.toList

    val hashtags = scalaList.map{ row =>
      val hashtags = row.getFamilyMap(_b("hashtags")).keySet().map { x =>
        val column = _s(x)
        column.split(":")(0)
      }

      hashtags

//      hashtags.map{ hashtag =>
//        val lat = _d(row.getValue(_b("hashtags"), _b(hashtag + ":latitude")))
//        val lon = _d(row.getValue(_b("hashtags"), _b(hashtag + ":longitude")))
//
//        Map("hashtag" -> hashtag, "lat" -> lat.toString, "lon" -> lon.toString)
//      }
    }

    val occurrences = scalaList.map(x => _i(x.getRow).toString).toList
    println(occurrences)
    val map = occurrences.zip(hashtags).toMap
    println(map)

    occurrences.map{ occ =>
      val set = map(occ)
      set.map{ value =>
        TopHashtag(occ.toInt, value)
      }
    }.flatten.take(amount)
  }

  def get(hashtag : String) : Result = {
    val get = new Get(_b(hashtag))
    get.addFamily(_b("metrics"))
    get.addFamily(_b("temporal"))
    get.setMaxVersions(1)

    tableHashtags.get(get)
  }

}
