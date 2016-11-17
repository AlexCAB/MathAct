name := "MathAct Tools"

version := "0.2.0"

scalaVersion := "2.11.8"

val jdkVersion = "1.8"

javacOptions ++= Seq("-encoding", "UTF-8", "-source", jdkVersion, "-target", jdkVersion, "-Xlint:deprecation", "-Xlint:unchecked")

scalacOptions ++= Seq("-encoding", "UTF-8", s"-target:jvm-$jdkVersion", "-feature", "-language:_", "-deprecation", "-unchecked", "-Xlint")


libraryDependencies  ++= Seq(
  "net.sf.jchart2d"               %  "jchart2d"             % "3.3.2"
)

addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.0" cross CrossVersion.full)
