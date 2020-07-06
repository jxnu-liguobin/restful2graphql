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
  "org.scala-lang" % "scala-reflect" % "2.12.11",
  "org.apache.commons" % "commons-lang3" % "3.10",
  "com.google.guava" % "guava" % "29.0-jre"
)