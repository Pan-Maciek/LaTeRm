package gui

import scalafx.scene.layout.StackPane
import gui.Screen
import gui.drawable.CursorView
import java.{util => ju}
import config.UiConfig

class TerminalPanel extends StackPane {
  val screen     = new Screen()
  val cursorView = new CursorView(screen.terminal.cursor)
  val terminal   = screen.terminal

  children.addAll(screen, cursorView)

  val timer = new ju.Timer()
  val task = new ju.TimerTask {
    def run() = {
      if (terminal.modified) {
        screen.redraw()
      } else {
        screen.partialDraw()
      }
      cursorView.onUpdate()
    }
  }
  timer.schedule(task, 500L, UiConfig.updatePeriod)
}
