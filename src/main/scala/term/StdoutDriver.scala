package term

import java.io.InputStream

import gui.data.LinesBuffer
import scalafx.application.Platform


case class StdoutDriver(val terminal: Terminal, val linesBuffer: LinesBuffer, val input: InputStream) {
  new Thread(() => {
    for (action <- ActionParser.parse(input)) {
      action match {
        case Write(char) => linesBuffer.write(char)
        case SetTitle(title) => Platform.runLater(() => terminal.title.set(title))
        case _ => println(action)
      }
      terminal.update()
    }
  }).start()
}
