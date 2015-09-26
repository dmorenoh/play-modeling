name := "play-modeling"

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.7"

libraryDependencies ++= Seq(
  cache,
  ws,
  evolutions,
  "org.scalaz"        %% "scalaz-core"            % "7.1.4",
  "com.typesafe.play" %% "play-slick"             % "1.1.0-M2",
  "com.typesafe.play" %% "play-slick-evolutions"  % "1.1.0-M2",
  "com.h2database"    % "h2"                      % "1.4.189",
  "org.scalatestplus" %% "play"                   % "1.4.0-M4"  % Test,
  "org.scalacheck"    %% "scalacheck"             % "1.12.5"    % Test
)

scalacOptions ++= Seq(
  "-deprecation",
  "-encoding", "UTF-8",       // yes, this is 2 args
  "-language:existentials",
  "-language:higherKinds",
  "-language:implicitConversions"
)


resolvers += "scalaz-bintray" at "http://dl.bintray.com/scalaz/releases"

// Play provides two styles of routers, one expects its actions to be injected, the
// other, legacy style, accesses its actions statically.
routesGenerator := InjectedRoutesGenerator
