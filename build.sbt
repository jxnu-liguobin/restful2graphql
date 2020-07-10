name := "restful2graphql"

version := "0.1"

scalaVersion := "2.12.11"

organization := "io.growing.graphql"

val log4j2: Seq[ModuleID] = Seq(
  "org.apache.logging.log4j" %% "log4j-api-scala" % "11.0",
  "org.apache.logging.log4j" % "log4j-api" % "2.8.2",
  "org.apache.logging.log4j" % "log4j-core" % "2.8.2",
  "org.apache.logging.log4j" % "log4j-slf4j-impl" % "2.8.2")

// growingio 开源配置管理和服务注册发现
val dryad = Seq(
  "io.growing" %% "dryad-consul" % "1.0.10",
  "io.growing" %% "dryad-core" % "1.0.10"
)

libraryDependencies ++= Seq(
  "com.squareup.okhttp3" % "okhttp" % "4.7.2",
  "com.typesafe" % "config" % "1.4.0",
  "org.apache.commons" % "commons-lang3" % "3.10",
  "com.google.guava" % "guava" % "29.0-jre",
  "com.typesafe.akka" %% "akka-actor" % "2.5.12",
  "com.typesafe.akka" %% "akka-stream" % "2.5.12",
  "com.typesafe.akka" %% "akka-http" % "10.1.1",
  "com.typesafe.akka" %% "akka-http-spray-json" % "10.1.1",
  "com.typesafe.scala-logging" %% "scala-logging" % "3.9.2",
  "com.graphql-java" % "graphql-java" % "14.0",
  "com.google.code.gson" % "gson" % "2.8.6"
) ++ log4j2 ++ dryad