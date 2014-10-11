package org.hiddenconflict.bolt

import backtype.storm.tuple.Tuple
import storm.scala.dsl.StormBolt
import twitter4j.Status

/**
 * @author Andreas C. Osowski
 */
class PrintBolt extends StormBolt(outputFields = List()) {

  def execute(input: Tuple) = {
    val status = input.getValue(0).asInstanceOf[Status]
    println(input.getValue(0))
  }

}
