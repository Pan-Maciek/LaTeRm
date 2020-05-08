scalaVersion := "2.13.1"
name := "lterm"
version := "0.1"

resolvers += "pty4j" at "https://jetbrains.bintray.com/pty4j"

libraryDependencies += "org.jetbrains.pty4j" % "pty4j" % "0.9.6"
libraryDependencies += "org.scilab.forge" % "jlatexmath" % "1.0.7"
libraryDependencies += "org.scalafx" %% "scalafx" % "12.0.2-R18"

lazy val osName = System.getProperty("os.name") match {
  case n if n.startsWith("Linux")   => "linux"
  case n if n.startsWith("Mac")     => "mac"
  case n if n.startsWith("Windows") => "win"
  case _ => throw new Exception("Unknown platform!")
}

lazy val javaFXModules = Seq("base", "controls", "fxml", "graphics", "media", "swing", "web")
libraryDependencies ++= javaFXModules.map(m =>
  "org.openjfx" % s"javafx-$m" % "12.0.2" classifier osName
)

