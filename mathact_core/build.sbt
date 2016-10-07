name := "MathAct Core"

version := "0.2.0"

scalaVersion := "2.11.8"

val jdkVersion = "1.8"

javacOptions ++= Seq("-encoding", "UTF-8", "-source", jdkVersion, "-target", jdkVersion, "-Xlint:deprecation", "-Xlint:unchecked")

scalacOptions ++= Seq("-encoding", "UTF-8", s"-target:jvm-$jdkVersion", "-feature", "-language:_", "-deprecation", "-unchecked", "-Xlint")


libraryDependencies  ++= Seq(
  "com.typesafe.akka"             %% "akka-actor"           % "2.4.8",
  "org.scalafx"                   %% "scalafx"              % "8.0.102-R11",
  "org.scalafx"                   %% "scalafxml-core-sfx8"  % "0.2.2",
  "org.scala-lang.modules"        %% "scala-xml"            % "1.0.5",
  "org.scalatest"                 %% "scalatest"            % "3.0.0"          % "test",
  "com.typesafe.akka"             %% "akka-testkit"         % "2.4.8"          % "test"
)


addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.0" cross CrossVersion.full)


