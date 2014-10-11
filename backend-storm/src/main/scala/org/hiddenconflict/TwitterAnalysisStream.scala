package org.hiddenconflict

import java.util.{ Properties, Arrays }

import backtype.storm.Config
import backtype.storm.LocalCluster
import backtype.storm.generated.KillOptions
import backtype.storm.serialization.IKryoDecorator
import backtype.storm.topology.TopologyBuilder
import backtype.storm.utils.Utils
import com.esotericsoftware.kryo.Kryo
import com.twitter.chill.KryoSerializer
import com.twitter.tormenta.spout.TwitterSpout
import kafka.producer.ProducerConfig
import org.hiddenconflict.bolt.FilterTweetBolt
import org.hiddenconflict.utils.TwitterStreamClient
import storm.kafka.bolt.KafkaBolt

class KryoDecorator extends IKryoDecorator {
  override def decorate(k: Kryo): Unit = KryoSerializer.registerAll(k)
}
object TwitterAnalysisStream extends App with TwitterStreamClient {

  // XXX - parallelism settings
  val builder = new TopologyBuilder

  //- Websocket sample
  builder.setSpout("sample", new HelloWorldSpout())
  builder.setBolt("kafkaOut", new KafkaBolt[String, String]).shuffleGrouping("sample")
  //-

  builder.setSpout("twitterSample", TwitterSpout(twitterStreamFactory, 1000, "tweet"))

  // Step 1: Filter incoming tweets.
  builder.setBolt("filterTweet", new FilterTweetBolt(), 4).shuffleGrouping("twitterSample")
  //builder.setBolt("filter_one", new FilterTweetBolt).shuffleGrouping("twitter")

  val conf = new Config()
  conf.registerDecorator(classOf[KryoDecorator])

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

  Utils.sleep(10000)
  cluster.shutdown()
}
