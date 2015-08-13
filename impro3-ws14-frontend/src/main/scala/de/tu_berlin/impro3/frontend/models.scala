package de.tu_berlin.impro3.frontend

object Models {
  // Objects need for the Locations
  case class Location(zone: String, meta: LocationMetaData, metrics: List[LocationMetric])
  case class LocationMetaData(center: LatLon, unique_hashtags: Int, hashtag_occurrences: Int)
  case class LatLon(lat: Int, lon: Int)
  case class LocationMetric(location: String, spatial_impact: Double)

  case class SpatialImpact(distribution: IndexedSeq[Int], cities : Map[String, Long])
  case class MapLocation(value: Int, lat: Double, lon: Double, loc: String)
  case class LocationDetails(spatial_impact: SpatialImpact, entropy_focus_spread: List[HashtagMetrics])

  // Objects needed for the Hashtags
  case class HashtagMetrics(tag: String, occurrences: Int, focus_zone: String, focus: Double, entropy: Double,
                            spread: Double)
  case class TemporalItem(time_interval: Long, occurrences: Int, focus_zone: String, focus: Double, entropy: Double,
                          spread: Double)
  case class Hashtag(tag: String, metrics: HashtagMetrics, temporal: List[TemporalItem], locations: List[HashtagLocation])
  case class HashtagLocation(value: Int, lat : Double, lon: Double)

  case class TopHashtag(value: Int, hashtag: String)
}
