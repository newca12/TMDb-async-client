name := "TMDb-async-client"
organization := "org.edla"
version := "1.1.0"
scalaVersion := "2.12.0"
coverageExcludedPackages := "org.edla.tmdb.client.Usage"
scalacOptions ++= Seq(
  "-language:postfixOps",
  "-language:existentials",
  "-language:implicitConversions",
  //"-optimize",
  "-deprecation",
  "-encoding", // yes this
  "UTF-8", // is 2 args
  "-feature",
  "-unchecked",
  "-Xfatal-warnings",
  "-Xlint",
  "-Yno-adapted-args",
  "-Ywarn-dead-code",
  "-Ywarn-numeric-widen",
  "-Ywarn-value-discard",
  "-Xfuture"
)
//wartremoverErrors ++= Warts.unsafe
scalacOptions in (Compile, doc) ++= Seq("-diagrams", "-implicits")
scalafmtConfig in ThisBuild := Some(file(".scalafmt.conf"))
libraryDependencies ++= {
  val akkaV     = "2.4.14"
  val akkaHttpV = "10.0.0"
  Seq(
    "org.scala-lang.modules" %% "scala-async"          % "0.9.6",
    "com.typesafe.akka"      %% "akka-actor"           % akkaV,
    "com.typesafe.akka"      %% "akka-stream"          % akkaV,
    "com.typesafe.akka"      %% "akka-http-core"       % akkaHttpV,
    "com.typesafe.akka"      %% "akka-http"            % akkaHttpV,
    "com.typesafe.akka"      %% "akka-http-spray-json" % akkaHttpV,
    "org.scala-lang.modules" %% "scala-java8-compat"   % "0.8.0",
    "org.scalatest"          %% "scalatest"            % "3.0.1" % "test"
  )
}
licenses := Seq("GNU GPL v3" → url("http://www.gnu.org/licenses/gpl.html"))
homepage := Some(url("http://github.com/newca12/TMDb-async-client"))
//conflictWarning := ConflictWarning.disable
publishMavenStyle := true
publishTo := {
  val nexus = "https://oss.sonatype.org/"
  if (isSnapshot.value)
    Some("snapshots" at nexus + "content/repositories/snapshots")
  else
    Some("releases" at nexus + "service/local/staging/deploy/maven2")
}
//publishTo := Some(Resolver.file("file", new File(Path.userHome.absolutePath + "/.m2/repository")))
publishArtifact in Test := false
pomExtra := (
  <scm>
    <url>git@github.com:newca12/TMDb-async-client.git</url>
    <connection>scm:git:git@github.com:newca12/TMDb-async-client.git</connection>
  </scm>
  <developers>
    <developer>
      <id>newca12</id>
      <name>Olivier ROLAND</name>
      <url>http://www.edla.org</url>
    </developer>
  </developers>
)
