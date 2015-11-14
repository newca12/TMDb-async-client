//http://stackoverflow.com/questions/19201509/how-to-format-the-sbt-build-files-with-scalariform-automatically
//sbt build:scalariformFormat to format sbt configuration files
import scalariform.formatter.preferences._
import com.typesafe.sbt.SbtScalariform.ScalariformKeys._
import com.typesafe.sbt.SbtScalariform._

lazy val BuildConfig = config("build") extend Compile
lazy val BuildSbtConfig = config("buildsbt") extend Compile

noConfigScalariformSettings
inConfig(BuildConfig)(configScalariformSettings)
inConfig(BuildSbtConfig)(configScalariformSettings)
scalaSource in BuildConfig := baseDirectory.value / "project"
scalaSource in BuildSbtConfig := baseDirectory.value
includeFilter in (BuildConfig, format) := ("*.scala": FileFilter)
includeFilter in (BuildSbtConfig, format) := ("*.sbt": FileFilter)

format in BuildConfig := {
  val x = (format in BuildSbtConfig).value
  (format in BuildConfig).value
}

preferences := preferences.value.
  setPreference(RewriteArrowSymbols, true).
  setPreference(AlignParameters, true).
  setPreference(AlignSingleLineCaseStatements, true).
  setPreference(DoubleIndentClassDeclaration, true)

