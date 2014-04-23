name := "TMDb-async-client"

organization := "org.edla"

version := "0.5"

scalaVersion := "2.11.0"

scalacOptions ++= Seq("-unchecked", "-deprecation", "-feature", "-optimize")

scalacOptions in (Compile, doc) ++= Seq("-diagrams","-implicits")

org.scalastyle.sbt.ScalastylePlugin.Settings

resolvers += "spray" at "http://repo.spray.io"

libraryDependencies ++= Seq(
  "io.spray" %% "spray-client" % "1.3.1-20140423",
  "com.typesafe.akka" %% "akka-actor" % "2.3.2",
  "io.spray" %%  "spray-json" % "1.2.6",
  "org.scala-lang.modules" %% "scala-async" % "0.9.1",  
  "junit" % "junit" % "4.11" % "test",
  "org.scalatest" %% "scalatest" % "2.1.3" % "test"
)

ScoverageSbtPlugin.instrumentSettings

CoverallsPlugin.coverallsSettings

licenses := Seq("GNU GPL v3" -> url("http://www.gnu.org/licenses/gpl.html"))

homepage := Some(url("http://github.com/newca12/TMDb-async-client"))

publishMavenStyle := true

publishTo := {
  val nexus = "https://oss.sonatype.org/"
  if (isSnapshot.value)
    Some("snapshots" at nexus + "content/repositories/snapshots")
  else
    Some("releases"  at nexus + "service/local/staging/deploy/maven2")
}

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
				<version>3.1</version>
				<configuration>
					<source>1.8</source>
					<target>1.8</target>
				</configuration>
			</plugin>
			<plugin>
				<groupId>net.alchim31.maven</groupId>
				<artifactId>scala-maven-plugin</artifactId>
				<version>3.1.6</version>
				<executions>
					<execution>
						<id>compile</id>
						<goals>
							<goal>compile</goal>
							<goal>testCompile</goal>
						</goals>
					</execution>
				</executions>
			</plugin>
			<plugin>
				<groupId>org.apache.maven.plugins</groupId>
				<artifactId>maven-surefire-plugin</artifactId>
				<version>2.16</version>
				<configuration>
					<includes>
						<include>**/*Suite.class</include>
						<include>**/*Test.class</include>
						<include>**/*Tests.class</include>
						<include>**/*Spec.class</include>
						<include>**/*Specs.class</include>
					</includes>
				</configuration>
			</plugin>
		</plugins>
	</build>
	<reporting>
		<plugins>
			<plugin>
				<groupId>net.alchim31.maven</groupId>
				<artifactId>scala-maven-plugin</artifactId>
				<version>3.1.6</version>
			</plugin>
		</plugins>
	</reporting>
)
