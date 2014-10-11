package org.hiddenconflict.bolt

import java.util

import backtype.storm.task.{ OutputCollector, TopologyContext }
import backtype.storm.tuple.Tuple
import org.apache.log4j.Logger
import storm.scala.dsl.StormBolt
import twitter4j.Status

/**
 * @author Andreas C. Osowski
 */
class DumpStatusBolt extends StormBolt(outputFields = List()) {
  private[this] var logger: Logger = null

  override def prepare(conf: util.Map[_, _], context: TopologyContext, collector: OutputCollector): Unit = {
    logger = Logger.getLogger(getClass)
    super.prepare(conf, context, collector)
  }

  def execute(input: Tuple) = {
    val status = input.getValue(0).asInstanceOf[StatusContent]
    logger.info("Dumping: " + status)
  }

}
