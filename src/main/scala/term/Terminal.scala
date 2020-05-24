package term

import java.io.{InputStream, OutputStream}

import com.pty4j.{PtyProcess, WinSize}
import config.SystemConstants
import gui.data.{LinesBuffer, TerminalLine}
import scalafx.beans.property.{IntegerProperty, StringProperty}

import scala.jdk.CollectionConverters._

class Terminal {
  private val cmd = Array(SystemConstants.shell)
  private val env =
    Map("TERM" -> "xterm-color") ++ SystemConstants.environment

  private val pty                 = PtyProcess.exec(cmd, env.asJava)
  private val stdin: OutputStream = pty.getOutputStream
  private val stdout: InputStream = pty.getInputStream

  def write(char: Array[Byte]): Unit = stdin.write(char)
  val defaultWidth                   = 86
  val defaultHeight                  = 31
  pty.setWinSize(new WinSize(defaultWidth, defaultHeight))

  val title = new StringProperty(this, "title", "LaTeRm")
  val width: IntegerProperty = new IntegerProperty(this, "width", defaultWidth) {
    onChange { (_, _, _) => () } // TODO resizing logic
  }
  val height: IntegerProperty = new IntegerProperty(this, "height", defaultHeight) {
    onChange { (_, _, _) => () } // TODO resizing logic
  }

  val linesBuffer = LinesBuffer(width, height)

  def lines: Seq[TerminalLine]                         = linesBuffer.lastLines
  def changedLines: Seq[(TerminalLine, Boolean)]       = linesBuffer.lastLinesChanged
  def cursor: Cursor                                   = linesBuffer.cursor
  def modified: Boolean                                = linesBuffer.modified
  def cursorPosition: (Double, Double, Double, Double) = linesBuffer.cursorCoords

  val driver = StdoutDriver(this, linesBuffer, stdout)
}
