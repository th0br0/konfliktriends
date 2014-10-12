# Konfliktriends
(developed during [HackZurich 2014](http://hackzurich.com/14)

In our modern age, nation borders fortunately are of little importance to friendships. Many strong border-crossing bonds have been formed in the past century. However: what happens to these bonds when countries engage in armed conflict? Does such a development also cause these bonds to wither?

There are currently 4 major geopolitical situations in the public's awareness: the "fight against IS", the conflict between Russia & Ukraine, the protests in Hong Kong and the ongoing tensions between Israel and the Gaza strip. Inspired by the Hack4Good workshop we want to analyse the consequences of such a situation for those who are forcibly pulled into them because they live nearby, because they've got family connections to either party or because they simply have friends who are now often portrayed as "opponents".

For this we've developed a Twitter-based approach for automatically mapping these kinds of relationships based upon message sentiment and other factors.

## [Live demo](http://zurich.mkdir.name)
The live demo will only be available until about 10/14/14.


## How to run
* Install [ScalaStorm](https://github.com/velvia/ScalaStorm) locally ```sbt publishLocal```
* Configure Twitter credentials in [TwitterClient.scala](backend-storm/src/main/scala/org/konfliktriends/storm/utils/TwitterClient.scala)
* Configure Db in [Db.scala](backend-storm/src/main/scala/org/konfliktriends/storm/Db.scala)
* Start Kafka & Zookeeper 
* Run backend-storm: ```sbt backend-storm/run```
* Run backend-camel: ```sbt backend-camel/run```

##License
Copyright (c) 2014 Andreas C. Osowski
The software contained in this repository is licensed under the terms of the GPL2. Please see [LICENSE](LICENSE) for its terms. 
