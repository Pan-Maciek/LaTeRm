package term

import java.io.InputStream

import gui.data.LinesBuffer
import scalafx.application.Platform.runLater

case class StdoutDriver(
    terminal: Terminal,
    linesBuffer: LinesBuffer,
    input: InputStream
) {
  val thread = new Thread(() => {
    for (action <- ActionParser.parse(input)) {
      linesBuffer.synchronized {
      action match {
        case Write(char)           => linesBuffer.write(char)
        case SetTitle(title)       => runLater(() => terminal.title.set(title))
        case SetStyle(sgr)         => linesBuffer.cursor.style = linesBuffer.cursor.style.applySgr(sgr)
        case MoveCursor(x, y)      => linesBuffer.cursor.translate(x, y)
        case SetColumn(n)          => linesBuffer.cursor.setColumn(n)
        case SetCursor(y, x)       => linesBuffer.cursor.setPosition(y, x)
        case ToggleLatex           => linesBuffer.cursor.style = linesBuffer.cursor.style.toggleLatex
        case ClearDisplay(n)       => linesBuffer.eraseInDisplay(n)
        case ClearLine(n)          => linesBuffer.eraseInLine(n)
        case Bell                  => // play bell sound
        case SaveCursorPosition    => linesBuffer.cursor.savePosition()
        case RestoreCursorPosition => linesBuffer.cursor.restorePosition()
        case SetCursorVisibility(visible) => linesBuffer.cursor.setVisibility(visible)
        case Warn(cause)           => println(cause)
      }
      }
    }
  })
  thread.start()
}
