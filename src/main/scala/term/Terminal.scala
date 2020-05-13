package term

import java.io.{InputStream, OutputStream}

import com.pty4j.PtyProcess
import config.SystemConstants
import gui.data.{LinesBuffer, TerminalLine}
import scalafx.beans.property.{IntegerProperty, StringProperty}

import scala.jdk.CollectionConverters._

class Terminal {
  var foo: Unit => Unit = _
  def update(): Unit = {
    if (foo != null) foo()
  }
  def onUpdate(function: Unit => Unit) = foo = function

  private val cmd = Array(SystemConstants.shell)
  private val env =
    Map("TERM" -> "xterm-color") ++ SystemConstants.environment

  private val pty = PtyProcess.exec(cmd, env.asJava)
  private val stdin: OutputStream = pty.getOutputStream
  private val stdout: InputStream = pty.getInputStream

  def write(char: Int): Unit = stdin.write(char)

  val title = new StringProperty(this, "title", "LaTeRm")
  val width: IntegerProperty = new IntegerProperty(this, "width", pty.getWinSize.ws_col) {
    onChange { (_, oldValue, newValue) => () } // TODO resizing logic
  }
  val height: IntegerProperty = new IntegerProperty(this, "height", pty.getWinSize.ws_row) {
    onChange { (_, oldValue, newValue) => () } // TODO resizing logic
  }

  private val linesBuffer = LinesBuffer(width, height)

  def lines: Seq[TerminalLine] = linesBuffer.lines
  def cursor: Cursor = linesBuffer.cursor

  StdoutDriver(this, linesBuffer, stdout)
}
