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
  new Thread(() => {
    // redraw thread
    val timer = new Timer()
    val task  = new TimerTask { def run() = terminal.update() }
    timer.schedule(task, 500L, UiConfig.updatePeriod)

    for (action <- ActionParser.parse(input)) {
      action match {
        case Write(char)     => linesBuffer.write(char)
        case SetTitle(title) => Platform.runLater(() => terminal.title.set(title))
        case _               => println(action)
      }
    }
  }).start()
}
