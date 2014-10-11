package org.hiddenconflict.bolt

import java.util

import backtype.storm.task.{ OutputCollector, TopologyContext }
import backtype.storm.topology.IRichBolt
import backtype.storm.tuple.Tuple
import redis.clients.jedis.{ Jedis, JedisPoolConfig, JedisPool }
import storm.scala.dsl.StormBolt
import twitter4j.Status

/**
 * @author Andreas C. Osowski
 */

class FilterTweetBolt extends StormBolt(Map("twitter" -> List("tweet"))) {

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

  def updateRedisLocation(jedis: Jedis, status: Status) = if (hasAuthorLocation(status)) {
    jedis.setnx("" + status.getUser.getId, status.getUser.getLocation)
  }

  override def execute(input: Tuple) = {
    implicit val status = input.getValue(0).asInstanceOf[Status]

    val jedis = jedisPool.getResource

    updateRedisLocation(jedis, status)

    if (containsMentions && hasLocation) {
      if (hasTweetLocation) println(status.getGeoLocation)
      else println(status.getUser.getLocation)
      using anchor input emit status
    }

    jedisPool.returnResource(jedis)
  }
}
