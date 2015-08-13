package de.tu_berlin.impro3.flink.spatio_temporal_dynamics.parsers.jax

/** Representation of a JSON path, similarly to XPath for XML. */
sealed trait JsonPath {
  def parent:           JsonPath
  def / (next: String): JsonPath
  def /#(next: Int):    JsonPath
  /** Go back to parent. */
  def rewind = this match {
    case parent /# i => parent /# (i + 1)
    case parent /  _ => parent
    case Root        => Root
  }
}

sealed trait JsonPath_/ extends JsonPath {
  def /#(next: Int)    = new /#(this, next)
  def / (next: String) = next match {
    case "."  => this
    case ".." => parent
    case _    => new /(this, next)
  }
}

/** JSON object field. */
final case class / (parent: JsonPath, next: String) extends JsonPath_/ {
  override def toString = s"$parent$next/"
}

/** JSON array element. */
final case class /#(parent: JsonPath, next: Int)    extends JsonPath_/ {
  override def toString = s"$parent$next/"
}

/** The root path, which is its own parent. */
case object Root extends JsonPath_/ {
  val parent = this
  def apply(next: String): JsonPath = / (next)
  def apply(next: Int):    JsonPath = /#(next)
  override def toString = "/"
}
