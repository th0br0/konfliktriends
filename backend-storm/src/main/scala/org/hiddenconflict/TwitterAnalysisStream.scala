package org.hiddenconflict

import java.util.Arrays

import backtype.storm.Config
import backtype.storm.LocalCluster
import backtype.storm.serialization.IKryoDecorator
import backtype.storm.topology.TopologyBuilder
import backtype.storm.utils.Utils
import com.esotericsoftware.kryo.Kryo
import com.twitter.chill.KryoSerializer
import org.hiddenconflict.bolt.FilterTweetBolt
import org.hiddenconflict.spout.TwitterSampleSpout

class KryoDecorator extends IKryoDecorator {
  override def decorate(k: Kryo): Unit = KryoSerializer.registerAll(k)
}

object TwitterAnalysisStream extends App {
  val consumerKey = "OypeY1uGpzqZ8gQNzhT5pv9QF"
  val consumerSecret = "TVfHVBFW8HJXhYRNh7tkHTxggLmRAfMbCu2Y5jgBRneHIF0bSu"
  val accessToken = "17422249-FTPgN7WOoyY6gwJfHlh2ieH4z3SwpJCImLGIUYcqw"
  val accessTokenSecret = "A5ZjRgxwMc7cLACkCCRtwOaBjGbDdkBEs3vEoozK0IOb1"

  // XXX - parallelism settings
  val builder = new TopologyBuilder
  builder.setSpout("twitter", new TwitterSampleSpout)

  // Step 1: Filter incoming tweets.
  builder.setBolt("filterTweet", new FilterTweetBolt, 4).shuffleGrouping("raw_tweet", "twitter")
  //builder.setBolt("filter_one", new FilterTweetBolt).shuffleGrouping("twitter")

  val conf = new Config()
  conf.registerDecorator(classOf[KryoDecorator])
  val cluster = new LocalCluster()
  cluster.submitTopology("test", conf, builder.createTopology())

  Utils.sleep(15000)
  cluster.shutdown()
  System.exit(0)
}
