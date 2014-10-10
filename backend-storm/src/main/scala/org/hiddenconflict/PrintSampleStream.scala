package org.hiddenconflict

import java.util.Arrays

import backtype.storm.Config
import backtype.storm.LocalCluster
import backtype.storm.topology.TopologyBuilder
import backtype.storm.utils.Utils

import storm.starter.bolt.PrinterBolt
import storm.starter.spout.TwitterSampleSpout

object PrintSampleStream extends App {
  val consumerKey = args(0)
  val consumerSecret = args(1)
  val accessToken = args(2)
  val accessTokenSecret = args(3)
  val keyWords = args.toSeq.drop(4)
  val builder = new TopologyBuilder
  builder.setSpout("twitter", new TwitterSampleSpout(consumerKey, consumerSecret, accessToken, accessTokenSecret, keyWords.toArray))
  builder.setBolt("print", new PrinterBolt).shuffleGrouping("twitter")
  val conf = new Config()
  val cluster = new LocalCluster()
  cluster.submitTopology("test", conf, builder.createTopology())

  Utils.sleep(15000)
  cluster.shutdown()
}
