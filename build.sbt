import scalariform.formatter.preferences._

organization := "com.hkdsun"

name := "bookstore"

version := "0.1"

scalaVersion := "2.11.7"

scalacOptions := Seq("-unchecked", "-deprecation", "-encoding", "utf8")

libraryDependencies ++= Seq(
  "ch.qos.logback" % "logback-classic" % "1.1.2",
  "com.typesafe.akka" %% "akka-actor" % "2.3.11",
  "com.typesafe.akka" %% "akka-testkit" % "2.3.11",
  "com.typesafe.scala-logging" %% "scala-logging" % "3.1.0",
  "io.spray" %% "spray-can" % "1.3.2",
  "io.spray" %% "spray-http" % "1.3.2",
  "io.spray" %% "spray-json" % "1.3.2",
  "io.spray" %% "spray-routing" % "1.3.2",
  "org.mongodb" %% "casbah" % "2.8.1",
  "org.scalatest" % "scalatest_2.11" % "2.2.4"
)

libraryDependencies += "com.typesafe.scala-logging" % "scala-logging-slf4j_2.11" % "2.1.2"

libraryDependencies += "net.sourceforge.htmlcleaner" % "htmlcleaner" % "2.10"


Revolver.settings

scalariformSettings

ScalariformKeys.preferences := ScalariformKeys.preferences.value
  .setPreference(RewriteArrowSymbols, true)
  .setPreference(AlignParameters, true)
  .setPreference(AlignSingleLineCaseStatements, true)
  .setPreference(PlaceScaladocAsterisksBeneathSecondAsterisk, true)
  .setPreference(MultilineScaladocCommentsStartOnFirstLine, true)
