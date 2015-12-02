name := "TMDb-async-client"
organization := "org.edla"
version := "1.0.2"
scalaVersion := "2.11.7"
scalacOptions ++= Seq(
  "-language:postfixOps", "-language:existentials", "-language:implicitConversions",
  //"-optimize",
  "-deprecation",
  "-encoding", "UTF-8", // yes, this is 2 args
  "-feature",
  "-unchecked",
  //"-Xfatal-warnings",
  "-Xlint",
  "-Yno-adapted-args",
  "-Ywarn-numeric-widen",
  "-Ywarn-value-discard",
  "-Xfuture"
)
scalacOptions in (Compile, doc) ++= Seq("-diagrams", "-implicits")
libraryDependencies ++= {
  val akkaStreamV = "2.0-M2"
  Seq(
    "org.scala-lang.modules" %% "scala-async" % "0.9.5",
    "com.typesafe.akka" %% "akka-actor" % "2.4.1",
    "com.typesafe.akka" %% "akka-stream-experimental" % akkaStreamV,
    "com.typesafe.akka" %% "akka-http-core-experimental" % akkaStreamV,
    "com.typesafe.akka" %% "akka-http-experimental" % akkaStreamV,
    "com.typesafe.akka" %% "akka-http-spray-json-experimental" % akkaStreamV,
    "com.typesafe.akka" %% "akka-http-testkit-experimental" % akkaStreamV % "test",
    "org.scalatest" %% "scalatest" % "2.2.4" % "test"
  )
}
licenses := Seq("GNU GPL v3" -> url("http://www.gnu.org/licenses/gpl.html"))
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
//publishTo := Some(Resolver.file("file",  new File("/PATH_TO_LOCAL_newca12.github.com/releases/")))
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
