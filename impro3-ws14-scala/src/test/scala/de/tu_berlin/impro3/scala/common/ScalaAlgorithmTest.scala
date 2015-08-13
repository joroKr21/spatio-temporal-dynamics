package de.tu_berlin.impro3.scala.common

import java.nio.file.Paths

import _root_.de.tu_berlin.impro3.scala.Algorithm
import org.junit.Test

object ScalaAlgorithmTest {

  val resourcesPath = Paths.get(getClass.getResource("/dummy.txt").getFile).toAbsolutePath.getParent.toString
}

@Test
abstract class ScalaAlgorithmTest[A <: Algorithm] {

  def integrationTest(): Unit
}
