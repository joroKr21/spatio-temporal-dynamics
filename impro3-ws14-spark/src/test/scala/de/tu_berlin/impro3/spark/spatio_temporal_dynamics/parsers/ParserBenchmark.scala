package de.tu_berlin.impro3.spark.spatio_temporal_dynamics.parsers

import org.scalameter.api._

import scala.io.Source

object ParserBenchmark extends PerformanceTest.Quickbenchmark {
  val jsonParser = new JsonParser
  val jaxParser  = new JaxParser
  val files = Gen.single("file")("tweets.random-001000.json")
  val lines = for {
    file <- files
  } yield Source.fromFile {
    getClass.getClassLoader.getResource(file).getPath
  }.getLines().toList

  performance of "Parsing Tweets with parsers" in {
    performance of "JsonParser" in {
      measure method "parse" in {
        using(lines) in jsonParser.parse
      }
    }

    performance of "JaxParser" in {
      measure method "parse" in {
        using(lines) in jaxParser.parse
      }
    }
  }
}
