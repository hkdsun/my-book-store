organization := "com.hkdsun"

name := "bookstore"

version := "0.1"

scalaVersion := "2.10.5"

scalacOptions := Seq("-unchecked", "-deprecation", "-encoding", "utf8")

libraryDependencies ++= Seq(
  "ch.qos.logback" % "logback-classic" % "1.1.2",
  "com.typesafe" %% "scalalogging-slf4j" % "1.0.1",
  "com.typesafe.akka" %% "akka-actor" % "2.3.11",
  "io.spray" %% "spray-can" % "1.3.2",
  "io.spray" %% "spray-http" % "1.3.2",
  "io.spray" %% "spray-json" % "1.3.2",
  "io.spray" %% "spray-routing" % "1.3.2",
  "org.mongodb" %% "casbah" % "2.8.1",
  "org.slf4j" % "log4j-over-slf4j" % "1.7.1",
  "org.slf4j" % "slf4j-api" % "1.7.1"
)

Revolver.settings
