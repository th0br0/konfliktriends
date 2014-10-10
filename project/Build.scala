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
      "Twitter Maven" at "http://maven.twttr.com"
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

  val bijectionVersion = "0.6.3"
  val scaldingVersion = "0.11.2"
  val chillVersion = "0.4.0"
  val hadoopVersion = "2.4.0"
  val cascadingVersion = "2.5.6"
  val cascadingJdbcVersion = "2.5.4"
  // 2.5.4 jdbc uses cascading 2.5.5 -.-
  val mongoHadoopVersion = "1.3.0"
  val postgresVersion = "9.3-1102-jdbc41"
  val stormVersion = "0.9.2-incubating"
  val twitter4jVersion = "4.0.2"


  lazy val slf4jVersion = "1.6.6"
  lazy val log4jVersion = "1.2.17"

  def module(name: String) = {
    val id = "backend-%s".format(name)
    Project(id = id, base = file(id), settings = sharedSettings ++ Seq(
      Keys.name := id
    )
    )
  }

  lazy val backendStorm= module("storm").settings(
    scalacOptions ++= Seq(
            "-optimise"
    ),
    libraryDependencies += "org.twitter4j" % "twitter4j-stream" % twitter4jVersion
  )
}
