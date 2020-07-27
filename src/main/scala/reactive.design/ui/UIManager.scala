package reactive.design.ui

import monix.eval.Task
import monix.reactive.Observable
import reactive.design.data.{UIEvent, UIUpdate}

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
        val recent = data.recent()
        val coordinates = data.cursorCoordinates()

        panel.update(recent, coordinates)
      }
    }
  }

}
