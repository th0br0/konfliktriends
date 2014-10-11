package com.hiddenconflict.camel

import org.apache.camel.main.Main
import org.apache.camel.builder.RouteBuilder

/**
 * @author Andreas C. Osowski
 */
class KafkaStreamRoute extends RouteBuilder {
  override def configure(): Unit = from("kafka:localhost?zookeeperHost=localhost&groupId=defaultz&topic=websocket")
    //.to("websocket:0.0.0.0:9292/websocket?sendToAll=true")
    .to("stream:file?fileName=/tmp/twitter.json")
}

object CamelMain extends App {
  val streamRoute = new KafkaStreamRoute
  val main = new Main
  main.enableHangupSupport()
  main.addRouteBuilder(streamRoute)
  main.run()

}
