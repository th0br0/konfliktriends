package com.konfliktriends.camel

import org.apache.camel.main.Main
import org.apache.camel.builder.RouteBuilder

/**
 * @author Andreas C. Osowski
 */
class KafkaStreamRoute extends RouteBuilder {
  override def configure(): Unit = from("kafka:localhost?zookeeperHost=localhost&groupId=defaultz&topic=websocket")
    .to("websocket:0.0.0.0:9292/websocket?sendToAll=true&staticResources=classpath:html")
    .to("stream:file?fileName=/tmp/twitter.json")
}

class ProxyPythonRoute extends RouteBuilder {
  override def configure() = from("jetty:http://0.0.0.0:9292/python?matchOnUriPrefix=false")
    .to("jetty:http://localhost:1337/?bridgeEndpoint=true")
}

object CamelMain extends App {
  val main = new Main
  main.enableHangupSupport()
  main.addRouteBuilder(new KafkaStreamRoute)
  //main.addRouteBuilder(new ProxyPythonRoute)
  main.run()

}
