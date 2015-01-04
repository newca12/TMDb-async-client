name := "TMDb-async-client"

organization := "org.edla"

version := "0.7"

scalaVersion := "2.11.4"

scalacOptions ++= Seq("-unchecked", "-deprecation", "-feature", "-optimize")

scalacOptions in (Compile, doc) ++= Seq("-diagrams","-implicits")

resolvers += "ConJars" at "http://conjars.org/repo"

libraryDependencies ++= Seq(
  "io.spray" %% "spray-client" % "1.3.2",
  "com.typesafe.akka" %% "akka-actor" % "2.3.8",
  "io.spray" %%  "spray-json" % "1.3.1",
  "com.pragmasoft" %% "spray-funnel" % "1.0-spray1.3" exclude("io.spray", "spray-client"),
  "org.scala-lang.modules" %% "scala-async" % "0.9.2"
)

licenses := Seq("GNU GPL v3" -> url("http://www.gnu.org/licenses/gpl.html"))

homepage := Some(url("http://github.com/newca12/TMDb-async-client"))

//conflictWarning := ConflictWarning.disable

publishMavenStyle := true

//publishTo := {
//  val nexus = "https://oss.sonatype.org/"
//  if (isSnapshot.value)
//    Some("snapshots" at nexus + "content/repositories/snapshots")
//  else
//    Some("releases"  at nexus + "service/local/staging/deploy/maven2")
//}

publishTo := Some(Resolver.file("file",  new File(Path.userHome.absolutePath+"/.m2/repository")))

//publishTo := Some(Resolver.file("file",  new File("/PATH_TO_LOCAL_newca12.github.com/releases/")))

publishArtifact in Test := false

//pomIncludeRepository := { _ => false }

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
    <contributors> </contributors>
    <properties>
        <encoding>UTF-8</encoding>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
    </properties>
	<build>
		<sourceDirectory>src/main/scala</sourceDirectory>
		<testSourceDirectory>src/test/scala</testSourceDirectory>
		<plugins>
			<plugin>
				<groupId>org.apache.maven.plugins</groupId>
				<artifactId>maven-compiler-plugin</artifactId>
				<version>3.2</version>
				<configuration>
					<source>1.8</source>
					<target>1.8</target>
				</configuration>
			</plugin>
			<plugin>
				<groupId>net.alchim31.maven</groupId>
				<artifactId>scala-maven-plugin</artifactId>
				<version>3.2.0</version>
				<executions>
					<execution>
						<id>compile</id>
						<goals>
							<goal>compile</goal>
						</goals>
					</execution>
				</executions>
			</plugin>
		</plugins>
	</build>
	<reporting>
		<plugins>
			<plugin>
				<groupId>net.alchim31.maven</groupId>
				<artifactId>scala-maven-plugin</artifactId>
				<version>3.2.0</version>
			</plugin>
		</plugins>
	</reporting>
)
