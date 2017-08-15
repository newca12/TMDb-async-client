resolvers += "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots/"
//addSbtPlugin("org.scalastyle"                     %% "scalastyle-sbt-plugin" % "0.9.0")
addSbtPlugin("com.lucidchart"                     % "sbt-scalafmt"           % "1.10")
addSbtPlugin("org.scoverage" % "sbt-scoverage" % "1.5.1-SNAPSHOT")
//addSbtPlugin("org.wartremover"                    % "sbt-wartremover"        % "2.1.1")
//addSbtPlugin("com.thoughtworks.sbt-best-practice" % "sbt-best-practice"      % "latest.release")
addSbtPlugin("org.xerial.sbt" % "sbt-sonatype" % "2.0")
addSbtPlugin("com.jsuereth" % "sbt-pgp" % "1.1.0-M1")
