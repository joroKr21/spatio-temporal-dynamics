package de.tu_berlin.impro3.spark.spatio_temporal_dynamics.parsers

import org.scalameter.api._

import scala.io.Source

object ParserRegression extends PerformanceTest.OnlineRegressionReport {
  override val persistor = new SerializationPersistor
  val jsonParser = new JsonParser
  val jaxParser  = new JaxParser
  val files = Gen.single("file")("tweets.random-001000.json")
  val sizes = Gen.range("size")(100, 1000, 100)
  val lines = for {
    file <- files; size <- sizes
  } yield Source.fromFile {
    getClass.getClassLoader.getResource(file).getPath
  }.getLines().take(size).toList

  performance of "Parsing Tweets with parsers" in {
    performance of "JsonParser" in {
      measure method "parse" in {
        using(lines) config {
          exec.independentSamples -> 1
        } in jsonParser.parse
      }
    }

    performance of "JaxParser" in {
      measure method "parse" in {
        using(lines) config {
          exec.independentSamples -> 1
        } in jaxParser.parse
      }
    }
  }
}
