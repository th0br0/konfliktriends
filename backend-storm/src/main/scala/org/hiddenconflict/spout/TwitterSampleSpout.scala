package org.hiddenconflict.spout

import java.util
import java.util.concurrent.LinkedBlockingDeque

import backtype.storm.Config
import backtype.storm.utils.Utils
import org.hiddenconflict.utils.TwitterStreamClient
import storm.scala.dsl.StormSpout
import twitter4j.{ StallWarning, StatusDeletionNotice, StatusListener, Status }

/**
 * @author Andreas C. Osowski
 */
class TwitterSampleSpout extends StormSpout(Map("twitter" -> List("raw_tweet")), false) with TwitterStreamClient {
  lazy val queue = new LinkedBlockingDeque[Status](1000)
  val statusListener = new StatusListener {
    override def onStatus(status: Status): Unit = { queue.offer(status) }

    override def onStallWarning(warning: StallWarning): Unit = {}

    override def onDeletionNotice(statusDeletionNotice: StatusDeletionNotice): Unit = {}

    override def onScrubGeo(userId: Long, upToStatusId: Long): Unit = {}

    override def onTrackLimitationNotice(numberOfLimitedStatuses: Int): Unit = {}

    override def onException(ex: Exception): Unit = {}
  }

  setup({
    twitterStream.addListener(statusListener)
  })

  shutdown {
    twitterStream.shutdown()
  }

  override def getComponentConfiguration: util.Map[String, AnyRef] = {
    val cfg = new Config()
    cfg.setMaxTaskParallelism(1)
    return cfg
  }

  override def nextTuple(): Unit = {
    val status = queue.poll()
    status match {
      case null => Utils.sleep(50)
      case v => using toStream "tweets" emit v
    }
  }
}
