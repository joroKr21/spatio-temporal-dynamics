package de.tu_berlin.impro3.flink.spatio_temporal_dynamics.parsers

import de.tu_berlin.impro3.flink.spatio_temporal_dynamics._
import model.Tweet
import jax._

import java.text.SimpleDateFormat
import java.util.Locale

import org.json4s.native.JsonParser._

/** Event sequential access [[Tweet]] parser for JSON formatted input. */
class JaxParser extends Parser {
  val dateFormat  = new SimpleDateFormat("EEE MMM dd HH:mm:ss ZZZZZ yyyy",
                                         Locale.US)
  private val jax = new Jax
  jax.addCallback { // this can be broken into multiple callbacks
    case (state, Root/"text", StringVal(text)) =>
      state + ('text -> text)
    case (state, Root/"user"/"id_str", StringVal(user)) =>
      state + ('user -> user)
    case (state, Root/"created_at", StringVal(date)) =>
      state + ('time -> dateFormat.parse(date).getTime)
    case (state, Root/"entities"/"hashtags"/# _ /"text", StringVal(tag)) =>
      val tags = state.getOrElse('tags, Nil).as[List[String]]
      state + ('tags -> (tag.toLowerCase :: tags))
    case (state, Root/"coordinates"/"coordinates"/# 0, DoubleVal(lon)) =>
      state + ('lon -> lon)
    case (state, Root/"coordinates"/"coordinates"/# 1, DoubleVal(lat)) =>
      state + ('lat -> lat)
    case (state, Root/"place"/"place_type", StringVal(pType)) =>
      state + ('pType -> pType)
    case (state, Root/"place"/"name", StringVal(place)) =>
      state + ('place -> place)
  }

  /** Possibly parse a single [[Tweet]]. */
  def parse(json: String) = try {
    val state = jax.parse(json)
    val text  = state('text).as[String]
    val user  = state('user).as[String]
    val time  = state('time).as[Long]
    val tags  = state.getOrElse('tags, Nil).as[Seq[String]]
    val gps   = if (state.contains('lat) && state.contains('lon))
      Some(state('lat).as[Double], state('lon).as[Double]) else None
    val city  = if (state.contains('pType) && state.contains('place) &&
                    state('pType).as[String].toLowerCase == "city")
      Some(state('place).as[String]) else None
    Some(Tweet(text, user, time, city, tags, gps))
  } catch { case _: Exception => None }

  private implicit class Id[A](a: A) {
    def as[B] = a.asInstanceOf[B]
  }
}


