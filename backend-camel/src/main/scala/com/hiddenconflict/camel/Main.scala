package com.hiddenconflict.camel

import org.apache.camel.main.Main
import org.apache.camel.builder.RouteBuilder

/**
 * @author Andreas C. Osowski
 */
class KafkaStreamRoute extends RouteBuilder {
  override def configure(): Unit = from("kafka:localhost?zookeeperHost=localhost&groupId=default&topic=websocket").to("stream:file?fileName=/tmp/kafka.txt")
}

object CamelMain extends App {
  val streamRoute = new KafkaStreamRoute
  val main = new Main
  main.enableHangupSupport()
  main.addRouteBuilder(streamRoute)
  main.run()

}
