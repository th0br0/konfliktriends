package summingbird

import sbt._
import Keys._
import scalariform.formatter.preferences._
import com.typesafe.sbt.SbtScalariform._
import com.twitter.scrooge.ScroogeSBT

import sbtassembly.Plugin._
import AssemblyKeys._

object HiddenConflictBuild extends Build {
  val extraSettings = Project.defaultSettings ++ scalariformSettings

  val sharedSettings = extraSettings ++ Seq(
    organization := "com.hiddenconflict",
    version := "0.0.1",
    scalaVersion := "2.10.4",
    javacOptions ++= Seq("-source", "1.7", "-target", "1.7"),

    libraryDependencies ++= Seq(
      "org.slf4j" % "slf4j-api" % slf4jVersion,
      "org.apache.storm" % "storm-core" % stormVersion,
      "org.apache.storm" % "storm" % stormVersion,
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
  val twitter4jVersion = "4.0.2"
  val jedisVersion = "2.6.0"

  val chillVersion = "0.5.0"
  val algebirdVersion = "0.8.1"
  val bijectionVersion = "0.7.0"
  val tormentaVersion = "0.8.0"



  lazy val slf4jVersion = "1.6.6"
  lazy val log4jVersion = "1.2.17"

  def module(name: String) = {
    val id = "backend-%s".format(name)
    Project(id = id, base = file(id), settings = sharedSettings ++ Seq(
      Keys.name := id
    )
    )
  }

  lazy val backendStorm = module("storm").settings(
    scalacOptions ++= Seq(
      "-optimise"
    ),
    libraryDependencies ++= Seq(
      "org.twitter4j" % "twitter4j-stream" % twitter4jVersion,
      "org.twitter4j" % "twitter4j-core" % twitter4jVersion,
      "redis.clients" % "jedis" % jedisVersion,

      "com.twitter" % "chill-java" % chillVersion,
      "com.twitter" %% "chill" % chillVersion,
      "com.twitter" %% "algebird-bijection" % algebirdVersion,
      "com.twitter" %% "bijection-netty" % bijectionVersion,
      "com.twitter" %% "tormenta" % tormentaVersion

    )
  )
}
