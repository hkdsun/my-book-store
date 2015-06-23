organization := "com.hkdsun"

name := "bookstore"

version := "0.1"

scalaVersion := "2.10.5"

scalacOptions := Seq("-unchecked", "-deprecation", "-encoding", "utf8")

libraryDependencies ++= Seq(
  "io.spray" %% "spray-can" % "1.3.2",
  "io.spray" %% "spray-http" % "1.3.2",
  "io.spray" %% "spray-routing" % "1.3.2",
  "io.spray" %% "spray-json" % "1.3.2",
  "com.typesafe.akka" %% "akka-actor" % "2.3.11",
  "org.mongodb" %% "casbah" % "2.8.1",
  "ch.qos.logback" % "logback-classic" % "1.1.2"
)

Revolver.settings
