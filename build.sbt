name := "play-modeling"

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.7"


libraryDependencies ++= Seq(
  cache,
  ws,
  evolutions,
  "org.scalaz"        %% "scalaz-core"            % "7.1.5",
  "com.typesafe.play" %% "play-slick"             % "1.1.1",
  "com.ticketfly"     %% "play-liquibase"         % "1.0",
  "com.h2database"    % "h2"                      % "1.4.189",
  "org.scalatestplus" %% "play"                   % "1.4.0"     % Test,
  "org.scalacheck"    %% "scalacheck"             % "1.12.5"    % Test
)

scalacOptions ++= Seq(
  "-deprecation",
  "-encoding", "UTF-8",       // yes, this is 2 args
  "-language:existentials",
  "-language:higherKinds",
  "-language:implicitConversions"
)

// Play provides two styles of routers, one expects its actions to be injected, the
// other, legacy style, accesses its actions statically.
routesGenerator := InjectedRoutesGenerator

scalacOptions ++= Seq(
  "-feature",
  "-deprecation",
  "-encoding", "UTF-8",
  "-unchecked",
  "-Xfatal-warnings",
  "-Yno-adapted-args",
  "-Ywarn-dead-code",
  "-Ywarn-numeric-widen",
  "-Ywarn-value-discard",
  "-Xfuture",
  "-language:existentials",
  "-language:higherKinds",
  "-language:postfixOps",
  "-language:implicitConversions"
)
