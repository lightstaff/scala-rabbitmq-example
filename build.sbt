def commonSettings(_name: String) = Seq(
  scalaVersion := "2.12.6",
  scalacOptions ++= Seq(
    "-deprecation",
    "-feature",
    "-unchecked",
    "-language:_",
    "-Xlint",
    "-Xfatal-warnings",
    "-Ywarn-dead-code",
    "-Ywarn-numeric-widen",
    "-Ywarn-unused",
    "-Ywarn-unused-import",
    "-Ywarn-value-discard"
  ),
  version := "0.1-SNAPSHOT",
  name := _name
)

lazy val common = (project in file("common"))
  .settings(commonSettings("scala-rabbitmq-example-common"))
  .settings(
    libraryDependencies ++= Seq(
      "io.spray" %% "spray-json" % "1.3.4"
    )
  )

lazy val publisher = (project in file("publisher"))
  .settings(commonSettings("scala-rabbitmq-example-publisher"))
  .settings(
    libraryDependencies ++= Seq(
      "com.rabbitmq" % "amqp-client" % "5.2.0",
      "io.spray" %% "spray-json" % "1.3.4",
      "com.typesafe" % "config" % "1.3.2",
      "ch.qos.logback" % "logback-classic" % "1.2.3"
    )
  )
  .dependsOn(common)

lazy val consumer = (project in file("consumer"))
  .settings(commonSettings("scala-rabbitmq-example-consumer"))
  .settings(
    libraryDependencies ++= Seq(
      "com.rabbitmq" % "amqp-client" % "5.2.0",
      "io.spray" %% "spray-json" % "1.3.4",
      "com.typesafe" % "config" % "1.3.2",
      "ch.qos.logback" % "logback-classic" % "1.2.3"
    )
  )
  .dependsOn(common)