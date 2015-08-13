package de.tu_berlin.impro3.scala

import de.tu_berlin.impro3.core.{Algorithm => CoreAlgorithm}

object Algorithm {

  object Command

  abstract class Command[A <: Algorithm](implicit m: scala.reflect.Manifest[A]) extends CoreAlgorithm.Command

}

abstract class Algorithm(val sparkMaster: String) extends CoreAlgorithm

