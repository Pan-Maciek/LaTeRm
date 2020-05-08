object Config {
  val WIDTH  = 400
  val HEIGHT = 600
}

object SystemConstants {
  import java.util._
  import scala.collection.JavaConverters._

  lazy val environment = System.getenv.asScala
  lazy val systemShell = {
    if (osName.indexOf("win") >= 0) {
      "c:\\windows\\system32\\cmd.exe"
    } else {
      throw new RuntimeException("Os not supported!")
    }
  }

  // It does not work
  lazy val wslPath        = "C:\\Windows\\System32\\wsl.exe"
  lazy private val osName = System.getProperty("os.name").toLowerCase()
}
