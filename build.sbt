name := "streambot"

resolvers ++= Seq(
  Resolver.sonatypeRepo("releases"),
  Resolver.sonatypeRepo("snapshots")
)

libraryDependencies ++= Seq(
  "com.chuusai" %% "shapeless" % "2.3.3", 
  "org.xerial" % "sqlite-jdbc" % "3.25.2" ,
  "com.typesafe.akka" %% "akka-http"   % "10.1.8",
  "com.typesafe.akka" %% "akka-stream" % "2.5.19",
  "com.typesafe.akka" %% "akka-http-spray-json" % "10.1.8",

  "com.typesafe.slick" %% "slick" % "3.3.1",
  "org.slf4j" % "slf4j-nop" % "1.6.4",
  "com.typesafe.slick" %% "slick-hikaricp" % "3.3.1",
  "org.xerial" % "sqlite-jdbc" % "3.8.11.2",
  "org.scalatest" %% "scalatest" % "3.0.8-RC4" % Test
)
lazy val settings = projectSettings

lazy val projectSettings =
  Seq(
    scalaVersion := "2.12.8",
    version := "0.0.1",
    organizationName := "Christophe Chen",
    startYear := Some(2019),
    licenses += ("MIT", url("http://opensource.org/licenses/MIT")),
    javacOptions ++= Seq("-source", "1.8"),
    scalacOptions ++= Seq(
      "UTF-8",
    )
  )
