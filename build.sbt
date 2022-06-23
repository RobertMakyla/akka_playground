
name := "akka_playground"

version := "1.0"

scalaVersion := "2.13.8"


libraryDependencies ++= {
  val AkkaVersion = "2.6.19"
  val slf4jVersion = "1.7.5"
  Seq(
    "com.typesafe.akka" %% "akka-actor-typed" % AkkaVersion,
    "com.typesafe.akka" %% "akka-actor-testkit-typed" % AkkaVersion % Test,
    "org.slf4j" % "slf4j-api" % slf4jVersion,
    "org.slf4j" % "slf4j-simple" % slf4jVersion
  )
}
