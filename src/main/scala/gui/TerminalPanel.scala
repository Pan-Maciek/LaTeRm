package gui

import scalafx.scene.layout.StackPane
import gui.Screen
import gui.drawable.CursorView
import term.Cursor
import java.{util => ju}
import config.UiConfig
import scalafx.scene.layout.Pane
import scalafx.scene.Group

class TerminalPanel extends Group {
  val screen       = new Screen()
  val terminal     = screen.terminal
  val curs: Cursor = terminal.cursor
  val cursorView   = new CursorView(curs)

  children.addAll(screen, cursorView)

  val timer = new ju.Timer()
  val task = new ju.TimerTask {
    def run() = {
      terminal.linesBuffer.synchronized {
        if (terminal.modified) {
          screen.redraw()
        } else {
          screen.partialDraw()
        }
      }

      // curs.viewCoords = terminal.cursorPosition
      // cursorView.onUpdate()
    }
  }
  timer.schedule(task, 500L, UiConfig.updatePeriod)
}
