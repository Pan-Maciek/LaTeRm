package term

import java.io.{InputStream, OutputStream}
import config.{SystemConstants}
import scala.jdk.CollectionConverters._

import com.pty4j.PtyProcess

class Terminal {
  private val cmd = Array(SystemConstants.shell)
  private val env =
    Map("TERM" -> "xterm-color") ++ SystemConstants.environment

  private val pty         = PtyProcess.exec(cmd, env.asJava)
  val stdin: OutputStream = pty.getOutputStream
  val stdout: InputStream = pty.getInputStream

  def width: Int = pty.getWinSize.ws_col
  def height: Int = pty.getWinSize.ws_row
}
