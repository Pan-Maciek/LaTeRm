scalaVersion := "2.13.1"
name := "laterm"
version := "1.0"

resolvers += "pty4j" at "https://jetbrains.bintray.com/pty4j"

libraryDependencies += "org.jetbrains.pty4j"      % "pty4j"                     % "0.9.6"
libraryDependencies += "org.scilab.forge"         % "jlatexmath"                % "1.0.7"
libraryDependencies += "org.scalafx"              %% "scalafx"                  % "12.0.2-R18"
libraryDependencies += "org.apache.logging.log4j" % "log4j-api"                 % "2.13.1"
libraryDependencies += "org.apache.logging.log4j" % "log4j-core"                % "2.13.1"
libraryDependencies += "org.scalactic"            %% "scalactic"                % "3.1.1"
libraryDependencies += "com.lihaoyi"              %% "fastparse"                % "2.3.0"
libraryDependencies += "org.scalatest"            %% "scalatest"                % "3.1.1" % "test"
libraryDependencies += "org.scalatestplus"        %% "scalatestplus-scalacheck" % "3.1.0.0-RC2" % Test

// FP!
libraryDependencies += "org.typelevel" %% "cats-core"      % "2.1.1"
libraryDependencies += "org.typelevel" %% "cats-effect"    % "2.1.4"
libraryDependencies += "io.monix"      %% "monix"          % "3.2.2"
libraryDependencies += "io.monix"      %% "monix-reactive" % "3.2.2"
libraryDependencies += "io.monix"      %% "monix-eval"     % "3.2.2"

lazy val osName = System.getProperty("os.name") match {
  case n if n.startsWith("Linux")   => "linux"
  case n if n.startsWith("Mac")     => "mac"
  case n if n.startsWith("Windows") => "win"
  case _                            => throw new Exception("Unknown platform!")
}

lazy val javaFXModules =
  Seq("base", "controls", "fxml", "graphics", "media", "swing", "web")
libraryDependencies ++= javaFXModules.map(m =>
  "org.openjfx" % s"javafx-$m" % "12.0.2" classifier osName
)

// This options ensure that building project fails when compiler issues warnings.
scalacOptions ++= Seq(
  "-unchecked",
  "-deprecation",
  "-Xfatal-warnings",
  "-language:postfixOps",
  "-language:higherKinds"
)

// force to use separate jvm for sbt
fork in run := true
