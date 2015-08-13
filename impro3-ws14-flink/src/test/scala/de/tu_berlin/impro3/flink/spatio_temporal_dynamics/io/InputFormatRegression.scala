package de.tu_berlin.impro3.flink.spatio_temporal_dynamics.io

import de.tu_berlin.impro3.flink.io._
import de.tu_berlin.impro3.flink.model.tweet.Tweet

import org.scalameter.api._

import scala.io.Source

object InputFormatRegression extends PerformanceTest.OnlineRegressionReport {
  override val persistor = new SerializationPersistor
  val   jsonInput = new JsonInputFormat
  val    jaxInput = new JaxInputFormat
  val  tweetInput = new TweetInputFormat
  val   gsonInput = new GsonTweetInputFormat
  val simpleInput = new SimpleTweetInputFormat
  val files = Gen.single("file")("tweets.random-001000.json")
  val sizes = Gen.range("size")(100, 1000, 100)
  val bytes = for {
    file <- files; size <- sizes
  } yield Source.fromFile {
      getClass.getClassLoader.getResource(file).getPath
    }.getLines().take(size).toList.map { _.getBytes }

  performance of "Parsing Tweets with io" in {
    performance of "JsonInputFormat" in {
      measure method "readRecord" in {
        using(bytes) config { exec.independentSamples -> 1 } in {
          _.map { json => jsonInput.readRecord(null, json, 0, json.length) }
        }
      }
    }

    performance of "JaxInputFormat" in {
      measure method "readRecord" in {
        using(bytes) config { exec.independentSamples -> 1 } in {
          _.map { json => jaxInput.readRecord(null, json, 0, json.length) }
        }
      }
    }

    performance of "TweetInputFormat" in {
      measure method "readRecord" in {
        using(bytes) config { exec.independentSamples -> 1 } in {
          _.map { json => tweetInput.readRecord(null, json, 0, json.length) }
        }
      }
    }

    performance of "GsonTweetInputFormat" in {
      measure method "readRecord" in {
        using(bytes) config { exec.independentSamples -> 1 } in {
          _.map { json => gsonInput.readRecord(null, json, 0, json.length) }
        }
      }
    }

    performance of "SimpleTweetInputFormat" in {
      measure method "readRecord" in {
        using(bytes) config { exec.independentSamples -> 1 } in {
          _.map { json =>
            simpleInput.readRecord(new Tweet, json, 0, json.length)
          }
        }
      }
    }
  }
}
