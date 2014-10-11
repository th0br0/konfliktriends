package org.hiddenconflict.bolt

import java.util

import backtype.storm.task.{ OutputCollector, TopologyContext }
import backtype.storm.topology.IRichBolt
import backtype.storm.tuple.Tuple
import redis.clients.jedis.{ Jedis, JedisPoolConfig, JedisPool }
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

class FilterTweetBolt extends StormBolt(outputFields = List(KafkaBolt.BOLT_KEY, KafkaBolt.BOLT_MESSAGE)) {

  // XXX - have localhost user-configured
  var jedisPool: JedisPool = null

  override def prepare(conf: util.Map[_, _], context: TopologyContext, collector: OutputCollector): Unit = {
    super.prepare(conf, context, collector)
    jedisPool = new JedisPool(new JedisPoolConfig, "localhost")
  }

  override def cleanup(): Unit = {
    jedisPool.close()
  }

  def containsMentions(implicit status: Status) = status.getUserMentionEntities.length > 0

  def hasTweetLocation(implicit status: Status) = status.getGeoLocation != null

  def hasAuthorLocation(implicit status: Status) = !status.getUser.getLocation.isEmpty

  def hasLocation(implicit status: Status) = hasTweetLocation || hasAuthorLocation

  def retrieveLocation(jedis: Jedis)(implicit status: Status): Option[String] = if (hasLocation) {
    if (hasTweetLocation) {
      Some(status.getGeoLocation.getLatitude + ";" + status.getGeoLocation.getLongitude)
    } else if (hasAuthorLocation) {
      Some(status.getUser.getLocation)
      /*jedis.get(status.getUser.getLocation) match {
        case null => jedis.setnx

        case v => Some(v)
      }                       */
    } else None
  } else None

  override def execute(input: Tuple) = {
    implicit val status = input.getValue(0).asInstanceOf[Status]

    if (containsMentions && hasLocation) {
      val jedis = jedisPool.getResource

      val location = retrieveLocation(jedis).getOrElse("None")
      val mentions = status.getUserMentionEntities.map(e => e.getScreenName)
      val author = status.getUser.getScreenName

      val result = FilterResult(status.getCreatedAt.getTime, author, mentions, location)
      val tolog = s"${result.timestamp},$author,${mentions.mkString(";")},$location\n"

      println(tolog)

      using anchor input emit ("websocket", DataObjectFactory.getRawJSON(status))

      jedisPool.returnResource(jedis)
    } else println("discard")

  }
}
