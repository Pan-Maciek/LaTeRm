import com.pty4j.PtyProcess
import scala.io.Source
import java.nio.charset.StandardCharsets
import collection.JavaConverters._

trait TerminalEmulator {
  def run: Unit
  def write: String => Unit
}

object TerminalEmulator {
  def go(): Unit = {
    new Thread(() => {
      val cmd =
        Array(SystemConstants.systemShell)
      val env =
        Map("TERM" -> "xterm-color") ++ SystemConstants.environment
      val pty = PtyProcess.exec(cmd, env.asJava)

      val stdout = Source.fromInputStream(pty.getInputStream)
      val stdin  = pty.getOutputStream

      def write(data: String): Unit = {}

      for (c <- stdout)
        print(c)

      val result = pty.waitFor
    }).start()
  }

}
