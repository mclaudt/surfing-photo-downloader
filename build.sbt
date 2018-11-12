name := "surfing-photo-downloader"

version := "0.1"

scalaVersion := "2.12.3"

val scrimageVersion = "2.1.8"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-http" % "10.0.10" withSources(),
  "com.sksamuel.scrimage" %% "scrimage-core" % scrimageVersion,
  "com.sksamuel.scrimage" %% "scrimage-io-extra" % scrimageVersion,
  "com.sksamuel.scrimage" %% "scrimage-filters" % scrimageVersion
)