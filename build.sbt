
name := "akka_playground"

version := "1.0"

scalaVersion := "2.13.8"


libraryDependencies ++= {
  val AkkaVersion = "2.6.19"
  Seq(
    "com.typesafe.akka" %% "akka-actor-typed" % AkkaVersion,
    "com.typesafe.akka" %% "akka-actor-testkit-typed" % AkkaVersion % Test
  )
}
