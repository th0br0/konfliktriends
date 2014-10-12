package org.hiddenconflict.spout

import backtype.storm.spout.SpoutOutputCollector
import backtype.storm.task.TopologyContext
import backtype.storm.topology.OutputFieldsDeclarer
import backtype.storm.topology.base.BaseRichSpout
import backtype.storm.tuple.{ Fields, Values }
import backtype.storm.utils.Time
import java.util.{ Map => JMap }
import java.util.concurrent.LinkedBlockingQueue
import com.twitter.tormenta.spout.Spout
import twitter4j._

/**
 * Storm Spout implementation for Twitter's streaming API.
 *
 * @author Sam Ritchie
 */

object TwitterSpout {
  val QUEUE_LIMIT = 1000
  // default max queue size.
  val FIELD_NAME = "tweet" // default output field name.

  def apply(
    factory: TwitterStreamFactory,
    limit: Int = QUEUE_LIMIT,
    fieldName: String = FIELD_NAME): TwitterSpout[Status] =
    new TwitterSpout(factory, limit, fieldName)(i => Some(i))
}

class TwitterSpout[+T](factory: TwitterStreamFactory, limit: Int, fieldName: String)(fn: Status => TraversableOnce[T])
    extends BaseRichSpout with Spout[T] {

  var stream: TwitterStream = null
  var collector: SpoutOutputCollector = null

  lazy val queue = new LinkedBlockingQueue[Status](limit)
  lazy val listener = new StatusListener {
    def onStatus(status: Status) {
      queue.offer(status)
    }

    def onDeletionNotice(notice: StatusDeletionNotice) {}

    def onScrubGeo(userId: Long, upToStatusId: Long) {}

    def onStallWarning(warning: StallWarning) {}

    def onTrackLimitationNotice(numberOfLimitedStatuses: Int) {}

    def onException(ex: Exception) {
      throw ex
    }
  }

  override def getSpout = this

  override def declareOutputFields(declarer: OutputFieldsDeclarer) {
    declarer.declare(new Fields(fieldName))
  }

  override def open(conf: JMap[_, _], context: TopologyContext, coll: SpoutOutputCollector) {
    collector = coll
    stream = factory.getInstance
    stream.addListener(listener)

    val filterQuery = new FilterQuery()
    // all israel + gaza
    //    filterQuery.locations(Array(Array(34.005768, 29.420600), Array(35.939361, 33.481987)))

    // israel + gaza + surroundings (:/): 31.31098, 34.114423 | 32.3724775, ta35.5102823
    // ukraine: 31.135254, 44.457310 |
    //
    filterQuery.locations(Array(
      Array(34.114423, 31.310980),
      Array(35.5102823, 32.3724775),
      Array(31.135254, 44.457310),
      Array(40.579166, 50.655875)
    ))
    stream.filter(filterQuery)
  }

  /**
   * Override this to change the default spout behavior if poll
   * returns an empty list.
   */
  def onEmpty: Unit = Time.sleep(50)

  override def nextTuple {
    Option(queue.poll).map(fn) match {
      case None => onEmpty
      case Some(items) => items.foreach { item =>
        collector.emit(new Values(item.asInstanceOf[AnyRef]))
      }
    }
  }

  override def flatMap[U](newFn: T => TraversableOnce[U]) =
    new TwitterSpout(factory, limit, fieldName)(fn(_).flatMap(newFn))

  override def close {
    stream.shutdown
  }
}
