package de.tu_berlin.impro3.scala.spatio_temporal_dynamics.parsers.jax

import java.io.Reader

import org.json4s.native.{ JsonParser => JP }
import JP._

/**
 * Event sequential access JSON parser.
 * Inspired by Java's SAX XML parser and XPath.
 */
class Jax {
  type State      = Map[Symbol, Any]      // alias
  type ~>[-A, +B] = PartialFunction[A, B] // alias
  private var callback: (State, JsonPath, Token) ~> State =
    { case (state, _, _) => state }
  /** Add a single mutating callback function. */
  def addCallback(f: (State, JsonPath, Token) ~> State) =
    callback = f.orElse(callback)
  /** Add a sequence of callback functions by squashing them in order. */
  def addCallbacks(fs: Seq[(State, JsonPath, Token) ~> State]) =
    fs.reverseIterator.foreach(addCallback)

  /**
   * Parse a [[String]].
   * @throws Exception on malformed JSON input.
   */
  def parse(json: String):   State = JP.parse(json,  parser)

  /**
   * Parse from a [[Reader]].
   * @throws Exception on malformed JSON input.
   */
  def parse(reader: Reader): State = JP.parse(reader, parser)

  private val parser: JP.Parser => State = p => {
    def parse = Stream.continually(p.nextToken)
      .takeWhile { _ != End }
      .foldLeft((Map.empty[Symbol, Any], Root: JsonPath)) {
      case ((state, path), token) => token match {
        case OpenObj  => state -> path
        case OpenArr  => state -> path /# 0
        case CloseObj => state -> path.rewind
        case CloseArr => state -> path.parent.rewind
        case FieldStart(field) => state -> path / field
        case _ => callback(state, path, token) -> path.rewind
      }
    }

    parse match {
      case (state, Root) => state // ok
      case _ => p.fail("Malformed JSON input")
    }
  }
}
