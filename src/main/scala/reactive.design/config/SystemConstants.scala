package reactive.design.config

object SystemConstants {
  import scala.jdk.CollectionConverters._

  lazy val environment = System.getenv.asScala
  lazy val shell = {
    osName match {
      case Windows => "c:\\windows\\system32\\cmd.exe"
      case Linux   => "/bin/bash"
      case Mac     => throw new Exception("Don't know where the bash is!")
    }
  }

  lazy val osName = System.getProperty("os.name") match {
    case n if n.startsWith("Linux")   => Linux
    case n if n.startsWith("Mac")     => Mac
    case n if n.startsWith("Windows") => Windows
    case _                            => throw new Exception("Unknown platform!")
  }
}

sealed trait Os
case object Windows extends Os
case object Linux   extends Os
case object Mac     extends Os
