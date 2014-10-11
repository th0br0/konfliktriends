package org.hiddenconflict.bolt

import java.util

import backtype.storm.task.{ OutputCollector, TopologyContext }
import backtype.storm.tuple.Tuple
import org.apache.log4j.Logger
import spray.json._
import storm.kafka.bolt.KafkaBolt
import storm.scala.dsl.StormBolt

/**
 * @author Andreas C. Osowski
 */

case class StatusJson(
  author: String,
  from: GeoCoordinate,
  to: Seq[GeoCoordinate],
  weight: Double = 0)

object StatusJsonProtocol extends DefaultJsonProtocol {
  implicit val geoCoordinateFormat = jsonFormat2(GeoCoordinate)
  implicit val statusJsonFormat = jsonFormat4(StatusJson)
}

class StatusToJsonBolt extends StormBolt(List(KafkaBolt.BOLT_KEY, KafkaBolt.BOLT_MESSAGE)) {
  var logger: Logger = null
  import StatusJsonProtocol._

  override def prepare(conf: util.Map[_, _], context: TopologyContext, collector: OutputCollector): Unit = {
    super.prepare(conf, context, collector)
    logger = Logger.getLogger(getClass)
  }

  override def execute(input: Tuple): Unit = {
    val status = input.getValueByField("statusGeo").asInstanceOf[StatusContent]

    logger.info("Emitting JSON")

    val statusJson = StatusJson(
      status.author,
      status.location.get.left.get,
      status.mentions.map(_.right.get),
      Math.random() * 2.0 - 1.0
    )

    using anchor input emit statusJson.toJson.toString
  }
}
