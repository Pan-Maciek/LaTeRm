package config

object SystemConstants {
  import java.util._
  import scala.jdk.CollectionConverters._

  lazy val environment = System.getenv.asScala
  lazy val shell = {
    osName match {
      case Windows => "c:\\windows\\system32\\cmd.exe"
      case Linux   => "/bin/bash"
      case Mac     => throw new Exception("Don't know where bash is!")
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
final case object Windows extends Os
final case object Linux   extends Os
final case object Mac     extends Os
