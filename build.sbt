name := "TMDb-async-client"
organization := "org.edla"
version := "1.0.5"
scalaVersion := "2.11.7"
coverageExcludedPackages := "org.edla.tmdb.client.Usage"
scalacOptions ++= Seq(
  "-language:postfixOps", "-language:existentials", "-language:implicitConversions",
  //"-optimize",
  "-deprecation",
  "-encoding", "UTF-8", // yes, this is 2 args
  "-feature",
  "-unchecked",
  "-Xfatal-warnings",
  "-Xlint",
  "-Yno-adapted-args",
  "-Ywarn-numeric-widen",
  "-Ywarn-value-discard",
  "-Xfuture"
)
scalacOptions in (Compile, doc) ++= Seq("-diagrams", "-implicits")
libraryDependencies ++= {
  val akkaV = "2.4.2"
  Seq(
    "org.scala-lang.modules" %% "scala-async" % "0.9.5",
    "com.typesafe.akka" %% "akka-actor" % akkaV,
    "com.typesafe.akka" %% "akka-stream" % akkaV,
    "com.typesafe.akka" %% "akka-http-core" % akkaV,
    "com.typesafe.akka" %% "akka-http-experimental" % akkaV,
    "com.typesafe.akka" %% "akka-http-spray-json-experimental" % akkaV,
    "com.lihaoyi" %% "acyclic" % "0.1.3" % "provided",
    "org.scalatest" %% "scalatest" % "2.2.6" % "test"
  )
}
addCompilerPlugin("com.lihaoyi" %% "acyclic" % "0.1.3")
testFrameworks += new TestFramework("utest.runner.Framework")
autoCompilerPlugins := true
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
