package org.hiddenconflict

import java.security.cert.X509Certificate
import java.util.{ Properties, Arrays }
import javax.net.ssl._

import backtype.storm.Config
import backtype.storm.LocalCluster
import backtype.storm.serialization.IKryoDecorator
import backtype.storm.topology.TopologyBuilder
import backtype.storm.utils.Utils
import com.esotericsoftware.kryo.Kryo
import com.twitter.chill.KryoSerializer
import org.hiddenconflict.bolt.{ DumpStatusBolt, GeocodeStatusBolt, FilterTweetBolt }
import org.hiddenconflict.spout.TwitterSpout
import org.hiddenconflict.utils.TwitterStreamClient
import storm.kafka.bolt.KafkaBolt

class KryoDecorator extends IKryoDecorator {
  override def decorate(k: Kryo): Unit = KryoSerializer.registerAll(k)
}

object TwitterAnalysisStream extends App with TwitterStreamClient {
  // XXX - parallelism settings
  Class.forName("org.postgresql.Driver");

  val builder = new TopologyBuilder
  builder.setSpout("twitterSample", TwitterSpout(twitterStreamFactory, 1000, "tweet"))

  // Step 1: Filter incoming tweets.
  builder.setBolt("filterTweet", new FilterTweetBolt()).shuffleGrouping("twitterSample")
  builder.setBolt("geocodeTweet", new GeocodeStatusBolt()).shuffleGrouping("filterTweet")
  //builder.setBolt("filter_one", new FilterTweetBolt).shuffleGrouping("twitter")

  builder.setBolt("dumpStatus", new DumpStatusBolt).shuffleGrouping("geocodeTweet")
  // Dump to file for now
  //builder.setBolt("kafkaOut", new KafkaBolt[String, String]).shuffleGrouping("filterTweet")

  val conf = new Config()
  conf.registerDecorator(classOf[KryoDecorator])

  // 33.481987, 35.939361 - 35.939361,33.481987
  // 29.420600, 34.005768 - 34.005768,29.420600

  //- Kafka config
  val props = new Properties();
  props.put("metadata.broker.list", "localhost:9092");
  props.put("request.required.acks", "1");
  props.put("serializer.class", "kafka.serializer.StringEncoder");
  conf.put(KafkaBolt.KAFKA_BROKER_PROPERTIES, props)
  conf.put(KafkaBolt.TOPIC, "websocket")

  //-

  val cluster = new LocalCluster()
  cluster.submitTopology("test", conf, builder.createTopology())

}
