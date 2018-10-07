resolvers += "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots/"
addSbtPlugin("org.scalastyle" %% "scalastyle-sbt-plugin" % "1.0.0")
addSbtPlugin("com.geirsson"   % "sbt-scalafmt"           % "1.6.0-RC2")
addSbtPlugin("org.scoverage"  % "sbt-scoverage"          % "1.5.1")
//addSbtPlugin("org.wartremover"                    % "sbt-wartremover"        % "2.1.1")
//addSbtPlugin("com.thoughtworks.sbt-best-practice" % "sbt-best-practice"      % "latest.release")
addSbtPlugin("org.xerial.sbt" % "sbt-sonatype" % "2.3")
addSbtPlugin("com.jsuereth"   % "sbt-pgp"      % "1.1.1")
