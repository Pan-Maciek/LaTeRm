package reactive.design

import reactive.design.data.UIEvent
import reactive.design.ui.Panel

import monix.reactive.Observable
import monix.eval.Task
import reactive.design.data.UIUpdate
import reactive.design.data.CursorDataPeek
import reactive.design.data.LinesBuffer
import reactive.design.data.TerminalLine

object UIManager {
  def apply(events: Observable[UIEvent]): (Panel, Task[Unit]) = {
    val manager = new UIManager()
    val task = events
      .mapEval(event => Task { manager.onEvent(event) })
      .completedL

    (manager.panel, task)
  }

}

private class UIManager() {
  val panel = new Panel()

  def onEvent(event: UIEvent) = {
    event match {
      case UIUpdate(data) => {
        val recent = data.getRecent()
        val coords = data.getCursorCoords()

        panel.update(recent, coords)
      }
    }
  }

}
