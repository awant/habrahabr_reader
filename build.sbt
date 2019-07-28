name := "habrahabr_reader"

version := "0.1"

scalaVersion := "2.12.8"

libraryDependencies ++= Seq(
	"org.scalatest" %% "scalatest" % "3.0.8" % "test",
	"com.github.pureconfig" %% "pureconfig" % "0.11.1",
	"com.typesafe.akka" %% "akka-actor" % "2.5.23",
)