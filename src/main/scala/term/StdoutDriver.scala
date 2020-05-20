package term

import java.io.InputStream
import java.util.{Timer, TimerTask}

import gui.data.LinesBuffer
import scalafx.application.Platform
import config.UiConfig

case class StdoutDriver(
    val terminal: Terminal,
    val linesBuffer: LinesBuffer,
    val input: InputStream
) {
  val thread = new Thread(() => {
    for (action <- ActionParser.parse(input)) {
      action match {
        case Write(char)      => linesBuffer.write(char)
        case SetTitle(title)  => Platform.runLater(() => terminal.title.set(title))
        case SetStyle(sgr)    => linesBuffer.cursor.style = linesBuffer.cursor.style.applySgr(sgr)
        case MoveCursor(x, y) => linesBuffer.cursor.translate(x, y)
        case SetColumn(n)     => linesBuffer.cursor.setColumn(n)
        case SetCursor(x, y)  => linesBuffer.cursor.setPosition(x, y)
        case ToggleLatex      => linesBuffer.cursor.style = linesBuffer.cursor.style.toggleLatex
        case _                => println(action)
      }
    }
  })
  thread.start()
}
