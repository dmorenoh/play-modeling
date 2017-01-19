name := "play-modeling"

version := "1.0"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.8"


libraryDependencies ++= Seq(
  cache,
  ws,
  "org.typelevel"           %% "cats"                   % "0.9.0",
  "com.typesafe.play"       %% "play-slick"             % "2.0.2",
  "com.ticketfly"           %% "play-liquibase"         % "1.3",
  "com.h2database"          % "h2"                      % "1.4.189",
  "org.scalatestplus.play"  %% "scalatestplus-play"     % "2.0.0-M1"  % Test,
  "org.scalacheck"          %% "scalacheck"             % "1.13.4"    % Test,
  "com.ironcorelabs"        %% "cats-scalatest"         % "2.2.0"     % Test
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
  // "-Ywarn-value-discard",
  "-Xfuture",
  "-language:existentials",
  "-language:higherKinds",
  "-language:postfixOps",
  "-language:implicitConversions"
)

// Have to do this because of scalatest async matchers
fork in Test := false

// show elapsed time
testOptions in Test += Tests.Argument(TestFrameworks.ScalaTest, "-oD")

coverageExcludedPackages := ".*Reverse.*;.*Routes.*;"

