package term

import java.io.{InputStream, OutputStream}
import config.{SystemConstants}
import collection.JavaConverters._

import com.pty4j.PtyProcess

class Terminal {
  private val cmd = Array(SystemConstants.shell)
  private val env =
    Map("TERM" -> "xterm-color") ++ SystemConstants.environment

  private val pty         = PtyProcess.exec(cmd, env.asJava)
  val stdin: OutputStream = pty.getOutputStream
  val stdout: InputStream = pty.getInputStream
}
