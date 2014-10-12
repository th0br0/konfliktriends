package summingbird

import sbt._
import Keys._
import scalariform.formatter.preferences._
import com.typesafe.sbt.SbtScalariform._
import com.twitter.scrooge.ScroogeSBT

import sbtassembly.Plugin._
import AssemblyKeys._

object KonfliktriendsBuild extends Build {
  val extraSettings = Project.defaultSettings ++ scalariformSettings ++ net.virtualvoid.sbt.graph.Plugin.graphSettings

  val sharedSettings = extraSettings ++ Seq(
    organization := "org.konfliktriends",
    version := "0.0.1",
    scalaVersion := "2.10.4",
    javacOptions ++= Seq("-source", "1.7", "-target", "1.7"),

    libraryDependencies ++= Seq(
      "org.slf4j" % "slf4j-api" % slf4jVersion,
      "com.github.velvia" %% "scala-storm" % "0.2.4-SNAPSHOT"
    ),

    resolvers ++= Seq(
      Opts.resolver.sonatypeSnapshots,
      Opts.resolver.sonatypeReleases,
      "Clojars Repository" at "http://clojars.org/repo",
      "Conjars Repository" at "http://conjars.org/repo",
      "Twitter Maven" at "http://maven.twttr.com",
      "twitter4j maven" at "http://twitter4j.org/maven2"
    ),

    parallelExecution in Test := true,

    scalacOptions ++= Seq(
      "-unchecked",
      "-deprecation",
      "-Yresolve-term-conflict:package"
    ),

    scalacOptions in Test ++= Seq("-Yrangepos")
  )

  lazy val formattingPreferences = {
    import scalariform.formatter.preferences._
    FormattingPreferences().
      setPreference(AlignParameters, false).
      setPreference(PreserveSpaceBeforeArguments, true)
  }

  lazy val backend = Project(
    id = "backend",
    base = file("."),
    settings = sharedSettings
  ).settings(
      test := {},
      publish := {}, // skip publishing for this root project.
      publishLocal := {}
    ).aggregate(
      backendStorm
    )

  val stormVersion = "0.9.2-incubating"
  val twitter4jVersion = "3.0.5"
  val jedisVersion = "2.6.0"

  val chillVersion = "0.5.0"
  val algebirdVersion = "0.8.1"
  val bijectionVersion = "0.7.0"
  val tormentaVersion = "0.8.0"
  val kafkaVersion = "0.8.1.1"
  val camelVersion = "2.14.0"
  val gmsVersion = "0.1.3"

  val slf4jVersion = "1.6.6"
  val log4jVersion = "1.2.17"
  val slickVersion = "2.1.0"
  val pgsqlVersion = "9.3-1102-jdbc4"
  val nlpVersion = "3.4.1"


  def module(name: String) = {
    val id = "backend-%s".format(name)
    Project(id = id, base = file(id), settings = sharedSettings ++ Seq(
      Keys.name := id
    )
    )
  }

  lazy val backendCamel = module("camel").settings(
    libraryDependencies ++= Seq(
      "org.slf4j" % "slf4j-simple" % slf4jVersion,
      "org.apache.camel" % "camel-core" % camelVersion,
      "org.apache.camel" % "camel-websocket" % camelVersion,
      "org.apache.camel" % "camel-jetty" % camelVersion,
      "org.eclipse.jetty.orbit" % "javax.servlet" % "3.0.0.v201112011016" artifacts (
        Artifact("javax.servlet", "jar", "jar")
        ),
      "org.apache.camel" % "camel-kafka" % camelVersion,
      "org.apache.camel" % "camel-stream" % camelVersion
    )
  )

  lazy val backendStorm = module("storm").settings(
    scalacOptions ++= Seq(
      "-optimise"
    ),
    libraryDependencies ++= Seq(
      "org.twitter4j" % "twitter4j-stream" % twitter4jVersion,
      "org.twitter4j" % "twitter4j-core" % twitter4jVersion,
      "redis.clients" % "jedis" % jedisVersion,

      "org.apache.storm" % "storm-core" % stormVersion,
      "org.apache.storm" % "storm" % stormVersion,
      "org.apache.storm" % "storm-kafka" % stormVersion,

      "com.google.maps" % "google-maps-services" % gmsVersion,
      "com.typesafe.slick" %% "slick" % slickVersion,
      "org.postgresql" % "postgresql" % pgsqlVersion,

      "io.spray" %% "spray-json" % "1.3.0",
      "edu.stanford.nlp" % "stanford-corenlp" % nlpVersion,
      "edu.stanford.nlp" % "stanford-parser" % nlpVersion,


      "com.twitter" % "chill-java" % chillVersion,
      "com.twitter" %% "chill" % chillVersion,
      "com.twitter" %% "algebird-bijection" % algebirdVersion,
      "com.twitter" %% "bijection-netty" % bijectionVersion,
      "com.twitter" %% "tormenta-twitter" % tormentaVersion,
      "org.apache.kafka" %% "kafka" % kafkaVersion

    )
  )
}
