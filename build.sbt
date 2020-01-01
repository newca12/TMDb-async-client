name := "TMDb-async-client"
organization := "org.edla"
version := "2.2.0"

scalaVersion in ThisBuild := "2.13.1"

coverageExcludedPackages := "org.edla.tmdb.client.Usage"
scalacOptions ++= Seq(
  "-deprecation", // Emit warning and location for usages of deprecated APIs.
  "-encoding",
  "utf-8",                         // Specify character encoding used by source files.
  "-explaintypes",                 // Explain type errors in more detail.
  "-feature",                      // Emit warning and location for usages of features that should be imported explicitly.
  "-language:existentials",        // Existential types (besides wildcard types) can be written and inferred
  "-language:higherKinds",         // Allow higher-kinded types
  "-language:implicitConversions", // Allow definition of implicit functions called views
  "-unchecked",                    // Enable additional warnings where generated code depends on assumptions.
  "-Xcheckinit",                   // Wrap field accessors to throw an exception on uninitialized access.
  //"-Xfatal-warnings",              // Fail the compilation if there are any warnings.
  "-Xlint:adapted-args",           // Warn if an argument list is modified to match the receiver.
  "-Xlint:constant",               // Evaluation of a constant arithmetic expression results in an error.
  "-Xlint:delayedinit-select",     // Selecting member of DelayedInit.
  "-Xlint:doc-detached",           // A Scaladoc comment appears to be detached from its element.
  "-Xlint:inaccessible",           // Warn about inaccessible types in method signatures.
  "-Xlint:infer-any",              // Warn when a type argument is inferred to be `Any`.
  "-Xlint:missing-interpolator",   // A string literal appears to be missing an interpolator id.
  "-Xlint:nullary-override",       // Warn when non-nullary `def f()' overrides nullary `def f'.
  "-Xlint:nullary-unit",           // Warn when nullary methods return Unit.
  "-Xlint:option-implicit",        // Option.apply used implicit view.
  "-Xlint:package-object-classes", // Class or object defined in package object.
  "-Xlint:poly-implicit-overload", // Parameterized overloaded implicit methods are not visible as view bounds.
  "-Xlint:private-shadow",         // A private field (or class parameter) shadows a superclass field.
  "-Xlint:stars-align",            // Pattern sequence wildcard must align with sequence component.
  "-Xlint:type-parameter-shadow",  // A local type parameter shadows a type already in scope.
  // "-Yno-imports",                      // No predef or default imports
  "-Ywarn-dead-code",        // Warn when dead code is identified.
  "-Ywarn-extra-implicit",   // Warn when more than one implicit parameter section is defined.
  "-Ywarn-numeric-widen",    // Warn when numerics are widened.
  "-Ywarn-unused:implicits", // Warn if an implicit parameter is unused.
  "-Ywarn-unused:imports",   // Warn if an import selector is not referenced.
  "-Ywarn-unused:locals",    // Warn if a local definition is unused.
  "-Ywarn-unused:params",    // Warn if a value parameter is unused.
  "-Ywarn-unused:patvars",   // Warn if a variable bound in a pattern is unused.
  "-Ywarn-unused:privates",  // Warn if a private member is unused.
  "-Ywarn-value-discard"     // Warn when non-Unit expression results are unused.
)
//wartremoverErrors ++= Warts.unsafe
scalacOptions in (Compile, doc) ++= Seq("-diagrams", "-implicits")
libraryDependencies ++= {
  val akkaV     = "2.6.1"
  val akkaHttpV = "10.1.11"
  Seq(
    "org.scala-lang.modules" %% "scala-async"          % "0.10.0",
    "com.typesafe.akka"      %% "akka-actor"           % akkaV,
    "com.typesafe.akka"      %% "akka-stream"          % akkaV,
    "com.typesafe.akka"      %% "akka-http-core"       % akkaHttpV,
    "com.typesafe.akka"      %% "akka-http"            % akkaHttpV,
    "com.typesafe.akka"      %% "akka-http-spray-json" % akkaHttpV,
    "com.typesafe.akka"      %% "akka-stream-contrib"  % "0.11",
    "org.scala-lang.modules" %% "scala-java8-compat"   % "0.9.0",
    "org.scalatest"          %% "scalatest"            % "3.1.0" % "test"
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
      <url>https://edla.org</url>
    </developer>
  </developers>
)
