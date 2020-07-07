name := "graphql-expand-scala"

version := "0.1"

scalaVersion := "2.12.11"

organization := "io.growing.graphql"


libraryDependencies ++= Seq(
  "com.squareup.okhttp3" % "okhttp" % "4.7.2",
  "com.typesafe" % "config" % "1.4.0",
  "com.fasterxml.jackson.module" %% "jackson-module-scala" % "2.11.0",
  "com.fasterxml.jackson.datatype" % "jackson-datatype-jdk8" % "2.11.0",
  "com.fasterxml.jackson.datatype" % "jackson-datatype-jsr310" % "2.11.0",
  "org.apache.commons" % "commons-lang3" % "3.10",
  "com.google.guava" % "guava" % "29.0-jre",
  "com.typesafe.akka" %% "akka-actor" % "2.5.12",
  "com.typesafe.akka" %% "akka-stream" % "2.5.12",
  "com.typesafe.akka" %% "akka-http" % "10.1.1",
  "com.typesafe.akka" %% "akka-http-spray-json" % "10.1.1"
)