name := "rest"

version := "1.0"

scalaVersion := "2.11.6"

libraryDependencies ++= Seq(
  "io.spray" %% "spray-can" % "1.3.3",
  "io.spray" %% "spray-http" % "1.3.3",
  "io.spray" %% "spray-routing" % "1.3.3",
  "com.typesafe.akka" %% "akka-actor" % "2.3.11",
  "com.typesafe.slick" %% "slick" % "3.0.0",
  "org.mongodb" %% "casbah" % "2.8.1",
  "ch.qos.logback" % "logback-classic" % "1.1.2",
  "net.liftweb" %% "lift-json" % "2.6.2"
)

