package org.konfliktriends.bolt

import java.util

import backtype.storm.task.{ OutputCollector, TopologyContext }
import backtype.storm.topology.IRichBolt
import backtype.storm.tuple.Tuple
import org.konfliktriends.utils.StatusHelpers
import storm.kafka.bolt.KafkaBolt
import storm.scala.dsl.StormBolt
import twitter4j.Status
import twitter4j.json.DataObjectFactory

/**
 * @author Andreas C. Osowski
 */

case class FilterResult(timestamp: Long,
  author: String,
  mentions: Seq[String],
  location: String)

case class StatusContent(
  time: Long,
  author: String,
  location: Option[Either[GeoCoordinate, String]],
  mentions: Seq[Either[Long, GeoCoordinate]],
  text: String,
  weight: Double = 0.0)

case class GeoCoordinate(lat: Double, lng: Double)

class FilterTweetBolt extends StormBolt(outputFields = List("status")) {

  import StatusHelpers._

  def containsMentions(implicit status: Status) = status.getUserMentionEntities.length > 0

  // XXX - Option of an Either? really?
  def retrieveLocation(status: Status): Option[Either[GeoCoordinate, String]] = if (status.hasLocation) {
    if (status.hasTweetLocation) {
      Option(status.getGeoLocation).map(l => Left(GeoCoordinate(l.getLatitude, l.getLongitude)))
    } else if (status.hasAuthorLocation) {
      Some(Right(status.getUser.getLocation))
    } else None
  } else None

  override def execute(input: Tuple) = {
    val status = input.getValueByField("tweet").asInstanceOf[Status]

    if (status.containsMentions && status.hasLocation) {
      val content = StatusContent(
        time = status.getCreatedAt.getTime,
        author = status.getUser.getScreenName,
        location = retrieveLocation(status),
        mentions = status.getUserMentionEntities.map(m => Left(m.getId)),
        text = status.getText()
      )

      using anchor input emit content

    } else println("discard")

  }
}
