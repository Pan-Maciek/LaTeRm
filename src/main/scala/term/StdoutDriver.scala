package term

import java.io.InputStream

import gui.data.LinesBuffer
import term.Terminal
import scalafx.application.Platform.runLater

case class StdoutDriver(
    terminal: Terminal,
    linesBuffer: LinesBuffer,
    input: InputStream
) {
  val thread = new Thread(() => {
    for (action <- ActionParser.parse(input)) {
      action match {
        case Write(char)           => linesBuffer.write(char)
        case SetTitle(title)       => runLater(() => terminal.title.set(title))
        case SetStyle(sgr)         => linesBuffer.cursor.style = linesBuffer.cursor.style.applySgr(sgr)
        case MoveCursor(x, y)      => linesBuffer.cursor.translate(x, y)
        case SetColumn(n)          => linesBuffer.cursor.setColumn(n)
        case SetCursor(x, y)       => linesBuffer.cursor.setPosition(x, y)
        case ToggleLatex           => linesBuffer.cursor.style = linesBuffer.cursor.style.toggleLatex
        case ClearDisplay(n)       => linesBuffer.eraseInDisplay(n)
        case ClearLine(n)          => linesBuffer.eraseInLine(n)
        case Bell                  => ??? // play bell sound
        case SaveCursorPosition    => linesBuffer.cursor.savePosition()
        case RestoreCursorPosition => linesBuffer.cursor.restorePosition()
      }
    }
  })
  thread.start()
}
