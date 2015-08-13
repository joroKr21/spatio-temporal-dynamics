package de.tu_berlin.impro3.flink.spatio_temporal_dynamics.io

import de.tu_berlin.impro3.flink.spatio_temporal_dynamics._
import model.Tweet
import parsers.JaxParser

import org.apache.flink.api.common.io.DelimitedInputFormat

import annotation.tailrec

class JaxInputFormat extends DelimitedInputFormat[Tweet] {
  val parser = new JaxParser

  override def nextRecord(record: Tweet): Tweet = {
    @tailrec def next: Tweet =
      try   { super.nextRecord(record)  }
      catch { case _: Exception => next }
    next
  }

  def readRecord(reuse: Tweet, bytes: Array[Byte],
                 offset: Int, numBytes: Int) =
    parser.parse(new String(bytes, offset, numBytes)).get
}
