package org.hiddenconflict

import backtype.storm.utils.Utils
import storm.kafka.bolt.KafkaBolt
import storm.scala.dsl.StormSpout

/**
 * @author Andreas C. Osowski
 */
class HelloWorldSpout extends StormSpout(List(KafkaBolt.BOLT_KEY, KafkaBolt.BOLT_MESSAGE)) {
  override def nextTuple(): Unit = {
    using toStream "default" emit ("websocket", "Hello World.")
    Utils.sleep(250)
  }
}
